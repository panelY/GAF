<template>
  <div class="app-container">
    <div class="page-left">
      <div class="tree-catalog">
        <span class="vertical-line">| </span>
        数据源分类
      </div>
      <gaf-tree-transparent
        ref="myGafTreeTransparent"
        :searchPlaceholder="searchPlaceholder2"
        :data-of-tree="dataOfTree1"
        :searchType="[0]"
        :expandedNodeKeys.sync="expandedNodeKeys2"
        :selectedKeys.sync="selectedNodeKeys2"
        @select="onSelect2"
      >
        <template v-slot:icon="{ iconNodeType }">
          <a-icon :type="iconNodeType.type === 2 ? tag : tags"></a-icon>
        </template>
      </gaf-tree-transparent>
    </div>
    <div class="page-right">
        <gaf-table-layout>
          <template #actions>
            <button @click="handleAdd" class="btn-fun blue">
              <span><a-icon type="plus" />
              新增</span>
            </button>
            <button @click="batchDel" class="btn-fun red">
              <span><a-icon type="delete" />
              批量删除</span>
            </button>
          </template>
          <template #filter>
            <div class="search-position" style="width:850px">
              <a-row type="flex" justify="end">
                <a-col :span="12">
                  <label style="font-size: 18px">时态：</label>
                  <a-range-picker
                    :show-time="{ format: 'HH:mm:ss' }"
                    :value="timeRange"
                    @change="onPickerChange"
                    @ok="onPickerOk"
                    size="large"
                    style="width:220px"
                    format="YYYY-MM-DD"
                  />
                </a-col>
                <a-col :span="8">
                  <a-input-search
                    @search="onSearch"
                    placeholder="请输入数据源别名查询"
                    size="large"
                  >
                    <button slot="enterButton" class="btn-search">
                      搜索
                    </button>
                  </a-input-search>
                </a-col>
              </a-row>
            </div>
          </template>
          <template #default>
            <gaf-table-with-page
              :pagination="pagination"
              :row-selection="{
                selectedRowKeys: selectedRowKeys,
                onChange: onSelectChange,
                onSelect: rowSelect,
                onSelectAll: rowSelectAll
              }"
              :data-source="sysResourceDatasourceList"
              :loading="loading"
              @change="tableChange"
              :row-key="(r, i) => r.datasourceId"
              :columns="columns.filter(item => item.dataIndex !== 'datasourceId')"
              style="width: 100%;"
              bordered
              size="small"
              align="center"
            >
              <template slot="databaseType" slot-scope="text">
                <span>
                  {{ databaseTypeMap.get(text) }}
                </span>
              </template>
              <template
                slot="customRender"
                slot-scope="text, record, index, column"
              >
                <span v-if="searchText && searchedColumn === column.key">
                  <template v-for="(fragment, i) in splitCellWithSearchText(text)">
                    <mark
                      v-if="fragment.toLowerCase() === searchText.toLowerCase()"
                      :key="i"
                      class="highlight"
                      >{{ fragment }}</mark
                    >
                    <template v-else>{{ fragment }}</template>
                  </template>
                </span>
                <template v-else>
                  {{ text }}
                </template>
              </template>
              <template slot="catalogType" slot-scope="text, record">
                {{ map.get(record.catalogCode) ? map.get(record.catalogCode) : '' }}
              </template>
              <template
                slot="addrandport"
                slot-scope="text, record"
              >
              {{ record.addr+':'+record.port }}
              </template>
              <template
                slot="operation"
                slot-scope="text, record"
                v-if="hasPKField"
              >
                <a @click.stop="() => handleDetail(record)" href="javascript:;" class="btn-view">
                  <a-icon type="profile" /> 详情
                </a>
                <!-- <a-divider type="vertical" />
                <a @click.stop="() => handleUpdate(record)" href="javascript:;" class="btn-edit">
                  <a-icon type="edit" /> 编辑
                </a> -->
                <a-divider type="vertical" />
                <a-popconfirm
                  @confirm="() => handleDelete(record)"
                  title="删除后无法恢复，确认是否继续?"
                  ok-text="确认"
                  cancel-text="取消"
                >
                  <a href="javascript:;" class="btn-del"><a-icon type="delete" /> 删除</a>
                </a-popconfirm>
              </template>
              <template slot="timeRender" v-if="timeFormat" slot-scope="text">
                {{ timeFormat(text) }}
              </template>
            </gaf-table-with-page>
          </template>
        </gaf-table-layout>
        <a-modal
          v-model="open"
          :width="800"
          :footer="null"
          :centered="true"
          @cancel="handleBack"
          destroy-on-close
        >
          <add-edit-form
            :title="title"
            :editData="editData"
            :data-of-tree="dataOfTree"
            @submit="handleSubmit"
            @back="handleBack"
            :operation="operation"
            :catalogCode="catalogCode"
          >
          </add-edit-form>
        </a-modal>
  </div>
  </div>
</template>

<script>
    import AddEditForm from '../../views/SpaceDatasource/AddOrEditForm'
    import moment from 'moment'
    import '~/assets/css/common.css'

    export default {
  components: {
    AddEditForm
  },
  data() {
    return {
      //树形组件是否选择
      isSecure: false,
      //是否选择时间选择器
      istime: false,
      //时间段信息
      timeRange: [],
      searchPlaceholder: '请输入模块名称查询',
      dataOfTree1: [],
      dataOfTree: [],
      map: new Map(),
      //搜索框Placeholder
      searchPlaceholder2: '请输入',
      expandedNodeKeys2: [],
      selectedNodeKeys2: [],
      // 搜索项
      searchKey: '',
      clearFilters: null,
      // 非多个禁用
      multiple: true,
      // 标题
      title: '',
      // 编辑记录
      editData: {},
      // 总条数
      total: 0,
      selectedRowKeys: [],
      // ${functionName}表格数据
      sysResourceDatasourceList: [],
      // 是否显示添加修改弹出层
      open: false,
      // 分页参数
      pagination: {
        pageSize: 10,
        current: 1,
        total: 0
      },
      // 列表是否加载中
      loading: true,
      searchText: '',
      searchText2: '',
      searchInput: null,
      searchedColumn: 'ds_name',
      sorter: {
        order: '',
        field: ''
      },
      // 详情：1，新增：2，编辑：3
      operation: 0,
      // 有无主键
      hasPKField: true,

      databaseTypeMap: new Map([
        ['1', 'POSTGRESQL'],
        ['4', 'MYSQL'],
        ['5', 'ORACLE'],
        ['6', 'SQLSERVER']
      ]),
      // 提供添加服务时使用，用做数据源分类展示
      catalogCode: '',
      // 左侧目录树添加所有类型节点
      searchAllNode: {key:'all',title:'所有类型',children:null,style: 'font-size: 18px;font-weight: bold',}
    }
  },
  computed: {
    columns: function() {
      const columns = [
        {
          title: '数据源id',
          dataIndex: 'datasourceId',
          key: 'datasource_id'
        },
        {
          title: '数据源别名',
          scopedSlots: {
            filterDropdown: 'filterDropdown',
            filterIcon: 'filterIcon',
            customRender: 'customRender'
          },
          dataIndex: 'dsName',
          key: 'ds_name',
          width: '135px'
        },
        {
          title: '数据源分类',
          dataIndex: 'catalogCode',
          key: 'catalog_code',
          scopedSlots: { customRender: 'catalogType' },
          width: '160px'
        },
        // {
        //   title: '类型',
        //   sorter: true,
        //   sortDirections: ['descend', 'ascend'],
        //   dataIndex: 'type',
        //   key: 'type',
        //   scopedSlots: { customRender: 'databaseType' }
        // },
        {
          title: '数据源类型',
          dataIndex: 'typeCode',
          key: 'type_code',
          width: '160px'
        },
        {
          title: '服务器地址',
          dataIndex: 'addr',
          key: 'addr',
          width: '400px'
        },
        {
          title: '数据库名称',
          dataIndex: 'dbName',
          key: 'db_name',
          width: '135px'
        },
        // {
        //   title: '端口',
        //   dataIndex: 'port',
        //   key: 'port'
        // },
        // {
        //   title: '服务器地址',
        //   scopedSlots: { customRender: 'addrandport' }
        // },


        // {
        //   title: '用户名',
        //   dataIndex: 'userName',
        //   key: 'user_name'
        // },
        // {
        //   title: '描述',
        //   dataIndex: 'description',
        //   key: 'description'
        // },
        {
          title: '时态',
          dataIndex: 'timeAttribute',
          key: 'time_attribute',
          scopedSlots: { customRender: 'timeRender' },
          width: '135px'
        },
        {
          title: '操作',
          scopedSlots: { customRender: 'operation' },
          width: '250px'
        }
      ]
      return this.hasPKField ? columns : columns.slice(0, columns.length - 2)
    },
    //时间格式
    timeFormat: function() {
      if (
        this.columns.filter(
          item =>
            item.scopedSlots && item.scopedSlots.customRender === 'timeRender'
        ).length > 0
      ) {
        return function(str) {
          if (!str || str === '') {
            return ''
          }
          const dt = new Date(str)
          const year = dt.getFullYear()
          let month = dt.getMonth() + 1
          let date = dt.getDate()

          month = month < 10 ? '0' + month : month
          date = date < 10 ? '0' + date : date


          return `${year}/${month}/${date}`
        }
      }
      return null
    }
  },
  created() {
    this.getDataOfTree()
    // this.defaultPicker() //设置默认时间
    this.getList()
  },
  methods: {
    moment,
    //搜索
    async onSearch(val) {
      // eslint-disable-next-line no-console
      this.searchText = val
      this.pagination.current = 1
      await this.getList()
    },
    //批量删除
    async batchDel() {
      const url = '/sys-mgt/sys-resource-datasources/'
      const selectedRowKeys = this.selectedRowKeys
      // eslint-disable-next-line no-console
      if (selectedRowKeys.length !== 0) {
        const rst = await this.$axios.delete(url, { data: selectedRowKeys })
        if (rst.data.isSuccessed) {
          this.$message.success('删除成功')
        } else {
          this.$message.error(`删除失败,原因:${rst.data.message}`)
        }
        this.$nextTick(() => {
          if (this.pagination.current !== 1 && selectedRowKeys.length === this.sysResourceDatasourceList.length){
            this.pagination.current--
          }
          this.getList()
        })
      } else {
        this.$message.warn('请选择您要删除的内容')
      }
    },
    rowSelect(record, selected, selectedRows) {
      // eslint-disable-next-line no-console
      console.log(record, selected, selectedRows)
    },
    rowSelectAll(selected, selectedRows, changeRows) {
      // eslint-disable-next-line no-console
      console.log(selected, selectedRows, changeRows)
    },
    // 根据搜索文本拆分单元格文本内容
    splitCellWithSearchText(text) {
      const str = text === null ? '' : text
      return str
        .toString()
        .split(
          new RegExp(`(?<=${this.searchText})|(?=${this.searchText})`, 'i')
        )
    },
    // 搜索查询
    handleSearch(selectedKeys, confirm, key, clearFilters) {
      if (this.searchedColumn !== key && this.clearFilters) this.clearFilters()
      confirm()
      this.searchText = selectedKeys[0]
      this.searchedColumn = key
      this.clearFilters = clearFilters
    },
    // 重置查询
    handleReset(clearFilters, key) {
      clearFilters()
      if (this.searchedColumn === key) {
        this.searchText = ''
        this.searchedColumn = ''
        this.clearFilters = null
      }
    },
    // 页码，排序项发生改变时，重新获取列表数据
    tableChange(pageInfo, filters, sorter) {
      if (pageInfo) {
        this.pagination.current = pageInfo.current
        this.pagination.pageSize = pageInfo.pageSize
      }
      if (sorter) {
        this.sorter.order = sorter.order === 'descend' ? 'DESC' : 'ASC'
        this.sorter.field = sorter.columnKey
      }
      this.getList()
    },
    // 添加数据
    handleAdd() {
      this.catalogCode = ''
      if(this.searchText2 && this.searchText2 !== '' && this.searchText2 !== 'all') {
        this.catalogCode = this.searchText2
      }
      this.open = true
      this.operation = 2
      this.title = '添加数据源'
    },
    // 添加修改提交后
    handleSubmit() {
      this.open = false
      this.editData = {}
      this.getList()
    },
    // 添加修改返回后
    handleBack() {
      this.editData = {}
      this.open = false
    },
    // 修改数据
    handleUpdate(row) {
      this.operation = 3
      this.open = true
      this.title = '修改数据源'
      this.editData = row
    },
    //详情展示
    handleDetail(row) {
      this.operation = 1
      this.open = true
      this.title = '详情展示'
      this.editData = row
    },
    // 删除数据
    async handleDelete(row) {
      const url = `/sys-mgt/sys-resource-datasources/` + row.datasourceId
      const rst = await this.$axios.delete(url)
      if (rst.data.isSuccessed) {
        this.$message.success('删除成功')
      } else {
        this.$message.error(`删除失败,原因:${rst.data.message}`)
      }
      this.$nextTick(() => {
        if (this.pagination.current !== 1 && this.sysResourceDatasourceList.length === 1){
          this.pagination.current--
        }
        this.getList()
      })
    },
    //表格选中项发生变化时的回调
    onSelectChange(selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
      if (this.selectedRowKeys.length > 0) {
        this.multiple = false
      } else {
        this.multiple = true
      }
    },
    //获取表单数据
    async getList() {
      this.loading = true
      let url = `/sys-mgt/sys-resource-datasources?pageSize=${this.pagination.pageSize}&pageNum=${this.pagination.current}&isSdx=true`
      if (this.searchText2 && this.searchText2 !== 'all') {
        url =
          url + '&catalogCode' + '=' +
          this.searchText2.trim()
        // this.isSecure = false
      }
      //模糊查询
      if (this.searchText2 !== 'all' && this.searchText.trim() && this.searchedColumn) {
        url =
          url +
          '&searchFieldName=' +
          this.searchedColumn +
          '&searchFieldValue=' +
          this.searchText.trim()
      }
      //通过事件段查询
      if (this.timeRange.length > 0){
        url =
          url +
          '&startTimeStamp=' +
          this.timeRange[0] +
          '&endTimeStamp=' +
          this.timeRange[1]
      }
      if (this.sorter.order && this.sorter.field) {
        url =
          url +
          '&orderFieldName=' +
          this.sorter.field +
          '&orderMethod=' +
          this.sorter.order
      }
      const res = await this.$axios.$get(url)
      this.loading = false
      if (res.isSuccessed) {
        this.pagination.current = res.data.pageIndex
        this.pagination.pageSize = res.data.pageSize
        this.pagination.total = res.data.total
        this.sysResourceDatasourceList = res.data.content
      } else {
        this.$message.error(`查询失败,原因:${res.message}`)
      }
    },
    //树形组件select事件
    onSelect2(selectedKeys, e) {
      this.isApiList =
        !e.node.dataRef.children || e.node.dataRef.children.length === 0
      this.searchText2 = ''
      if (!e.node.dataRef.children || e.node.dataRef.children.length === 0) {
        if(e.selected) {
          this.searchText2 = e.node.dataRef.key
        }
        this.getList()
      }
    },
    //获取数据源分类树形数据
    async getDataOfTree() {
      const url = `/sys-mgt/sys-dicts/DataSourceClassification/tree`
      const res = await this.$axios.$get(url)
      if (res.isSuccessed) {
        this.dataOfTree1.push(this.searchAllNode)
        this.dataOfTree1 = [...this.dataOfTree1,...this.changePropertyName(res.data)]
        this.getMap(res.data)
        this.dataOfTree = res.data
        this.getList()
      } else {
        this.$message.error('加载API分组树失败,原因：' + res.message)
      }
    },
    getMap(data) {
      data.forEach((element) => {
        this.map.set(element.value, element.label)
        if (element.children){
          this.getMap(element.children)
        }
      })
    },
    //时间选择器change事件
    onPickerChange(date) {
      this.timeRange.length = 0
      this.timeRange.push(...date)
    },
    //时间选择器点完ok重新加载列表
    onPickerOk() {
      // this.istime = true
      this.getList()
    },
    //默认时间
    defaultPicker() {
      const from = moment().subtract(1, 'days')
      const to = moment()
      this.timeRange.length = 0
      this.timeRange.push(from)
      this.timeRange.push(to)
    },
     //改变属性名
    changePropertyName(data) {
      let item = [];
      data.map((list) => {
        let newData = {};
        newData.key= list.value;
        newData.title = list.label;
        newData.children = list.children  ? this.changePropertyName(list.children) : [];    //如果还有子集，就再次调用自己
        item.push(newData);
      });
      return item;
    },
  }
}
</script>

<style scoped>
.app-container {
  height: 100%;
}
.add-user-btn {
  border: none;
  background-color: rgb(47, 117, 249);
  color: rgba(255, 255, 255, 0.7);
  box-shadow: 0 0 2px 2px rgba(84, 92, 100, 0.55);
  transition: linear 0.2s;
  box-shadow: 3px 3px 0 rgba(128, 128, 128, 0.3);
}
</style>
