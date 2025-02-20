<template>
  <div>
    <div style="margin-bottom: 10px">
      <a-select
        v-show="false"
        v-model="type"
        show-search
        placeholder="尝试搜一搜'地图服务'"
        option-filter-prop="children"
        style="width: 165px"
        :filter-option="filterOption"
        :default-value="'-1'"
      >
        <!--现在暂时不使用所有类型，若后面有需要直接绑定options-->
        <a-select-option value="-1">所有类型</a-select-option>
        <a-select-option value="RESTDATA">数据服务</a-select-option>
      </a-select>
      <a-select
        v-model="searchedColumn"
        placeholder="选择列名"
        style="width: 165px"
        default-value="name"
      >
        <a-select-option value="name">服务名称</a-select-option>
        <a-select-option value="address">服务地址</a-select-option>
      </a-select>
      <a-input-search
        v-model="searchValue"
        :placeholder="searchPlaceholder"
        style="width: 250px"
        allow-clear
        @search="onSearch"
      >
        <button slot="enterButton" class="btn-search" style="height: 32px"> 搜索 </button>
      </a-input-search>
    </div>
    <div>
      <gaf-table-with-page
        :show-x-h="false"
        :pagination="pagination"
        :row-selection="{
          onChange: onChange,
          selectedRowKeys: selectedRowKeys,
          onSelect: rowSelect,
          onSelectAll: rowSelectAll,
          columnWidth: '50px'
        }"
        :row-key="(r, i) => r.gisServiceId"
        :data-source="webgisServiceList"
        :loading="loading"
        :columns="columns"
        bordered
        size="small"
        @change="tableChange"
      >
        <template slot="timeRender" slot-scope="text">
          {{ timeFormat(text) }}
        </template>
        <template slot="type" slot-scope="text">
          {{
            webgisServiceTypes.get(text) ? webgisServiceTypes.get(text) : text
          }}
        </template>
        <template slot="address" slot-scope="text">
          <div class="url">
            <a-tooltip placement="topLeft">
              <template slot="title">
                {{ text }}
              </template>
              <a :href="text" target="_blank">{{ text }}</a>
            </a-tooltip>
          </div>
        </template>
      </gaf-table-with-page>
    </div>
  </div>
</template>
<script>
import '~/assets/css/common.css'
// @ts-nocheck
export default {
  props: {
    // 暂时不使用
    options: {
      type: Array,
      default: () => [],
    },
    webgisServiceTypes: {
      type: Map,
      default: () => new Map(),
    },
    webgisServiceId: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      webgisServiceList: [],
      // 搜索条件
      // 服务类型
      type: 'RESTDATA',
      searchValue: '',
      searchedColumn: 'name',
      // 分页参数
      pagination: {
        pageSize: 10,
        current: 1,
        total: 0,
      },
      sorter: {
        order: null,
        field: null,
      },
      // 列表是否加载中
      loading: true,
      // 选中的行key
      selectedRowKeys: [],
      columns: [
        {
          title: '服务名称',
          dataIndex: 'name',
          key: 'name',
        },
        {
          title: '服务类型',
          dataIndex: 'typeCode',
          key: 'type_code',
          scopedSlots: { customRender: 'type' },
        },
        {
          title: '服务地址',
          dataIndex: 'address',
          key: 'address',
          scopedSlots: { customRender: 'address' },
          align: 'center',
          width: '450px'
        },
        {
          title: '时态',
          dataIndex: 'timeAttribute',
          key: 'time_attribute',
          scopedSlots: { customRender: 'timeRender' },
        },
      ],
    }
  },
  computed: {
    searchPlaceholder() {
      if (this.searchedColumn === 'name') {
        return '请输入服务名称进行搜索'
      } else if (this.searchedColumn === 'address') {
        return '请输入服务地址进行搜索'
      } else {
        return '请输入值进行搜索'
      }
    },
  },
  created() {
    this.getList()
  },
  methods: {
    // 当搜索时
    onSearch() {
      this.getList()
    },
    // 当选择服务改变时
    onChange(selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.$emit('onSelectedChange', selectedRowKeys, selectedRows)
    },
    async getList() {
      this.loading = true
      let url = `/map/webgis-service-associations/unrelated/${this.webgisServiceId}?pageSize=${this.pagination.pageSize}&pageNum=${this.pagination.current}`
      if (this.type && this.type !== '-1') {
        url = url + '&typeCode=' + this.type
      }
      if (this.searchValue.trim() && this.searchedColumn) {
        url =
          url +
          '&searchFieldName=' +
          this.searchedColumn +
          '&searchFieldValue=' +
          this.searchValue.trim()
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
        this.pagination.current = res.data.pageNum
        this.pagination.pageSize = res.data.pageSize
        this.pagination.total = res.data.totalCount
        this.webgisServiceList = res.data.pageList
      } else {
        this.$message.error(`查询失败,原因:${res.message}`)
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
    // 筛选服务类型
    filterOption(input, option) {
      return option.componentOptions.children[0].text
        .toLowerCase()
        .includes(input.toLowerCase())
    },
    timeFormat(str) {
      if (!str || str === '') {
        return ''
      }
      const dt = new Date(str)
      const year = dt.getFullYear()
      let month = dt.getMonth() + 1
      let date = dt.getDate()
      let hour = dt.getHours()
      let minute = dt.getMinutes()
      let second = dt.getSeconds()

      month = month < 10 ? '0' + month : month
      date = date < 10 ? '0' + date : date
      hour = hour < 10 ? '0' + hour : hour
      minute = minute < 10 ? '0' + minute : minute
      second = second < 10 ? '0' + second : second

      return `${year}/${month}/${date} ${hour}:${minute}:${second}`
    },
  },
}
</script>

<style scoped>
.url {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 450px;
}

.url a {
  color: rgb(34, 34, 34);
  text-decoration: none;
}

.url a:link {
  color: rgb(34, 34, 34);
}

.url a:visited {
  color: rgb(153, 153, 153);
}

.url a:hover {
  color: #559df5;
}

.url a:active {
  color: rgb(153, 153, 153);
}
</style>
