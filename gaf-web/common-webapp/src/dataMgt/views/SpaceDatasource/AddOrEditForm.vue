<template>
  <div class="page-container">
    <template>
      <a-breadcrumb separator=">" class="modal-line">
        <span class="vertical-line">| </span>
        <a-breadcrumb-item class="text-bolder">{{ title }}</a-breadcrumb-item>
      </a-breadcrumb>
    </template>
    <a-tabs v-if="operation === 1" default-active-key="1" @change="callback">
      <a-tab-pane key="1" tab="基本信息">
        <div class="page-container-box">
      <a-form
        :form="addOrEditForm"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 13 }"
        layout="horizontal"
      >
         <a-form-item v-show="false" label="是否为空间数据库">
          <a-switch
            v-decorator="[
              'isSdx',
              { valuePropName: 'checked', initialValue: true }
            ]"
            checked-children="是"
            un-checked-children="否"
          />
        </a-form-item>
        <a-form-item v-if="isSpatialdb" label="数据源分类">
          <a-cascader
            :disabled="operation !== 3"
            v-decorator="[
              'catalogCode',
              {
                rules: [
                  {
                    required: true,
                    message: '数据源分类不能为空'
                  }
                ]
              }

            ]"
            :options="option3"
            :load-data="loadData"
            placeholder="请选择数据源分类"
            change-on-select
          />
        </a-form-item>
        <a-form-item label="数据源类型">
          <a-cascader
            :disabled="operation === 1"
            v-decorator="[
              'typeCode',
              {
                rules: [
                  {
                    required: true,
                    message: '数据源类型不能为空'
                  }
                ]
              }
            ]"
            :options="option4"
            :load-data="loadData"
            placeholder="请选择数据源类型"
            change-on-select
            @change="onChange4"
          />
        </a-form-item>

        <!-- 下面是非空间数据库 -->
        <a-form-item label="数据源别名">
          <a-input
            v-decorator="[
              'dsName',
              {
                rules: [
                  {
                    required: true,
                    message: '数据源名称不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入数据源名称"
            allow-clear
          />
        </a-form-item>
        <!-- <a-form-item v-if="!isSpatialdb" label="类型">
          <a-select
            :disabled="operation === 1"
            v-decorator="[
              'type',
              {
                rules: [
                  {
                    required: true,
                    message: '请选择类型'
                  }
                ],
                initialValue: '1'
              }
            ]"
            :options="options"
            placeholder="请选择类型"
            default-active-first-option
          >
          </a-select>
        </a-form-item> -->
        <a-form-item v-if="isfiletype" label="服务地址">
          <a-input
            :disabled="operation === 1"
            v-decorator="[
              'addr',
              {
                rules: [
                  {
                    required: true,
                    message: '服务地址不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入服务地址(ip+端口)"
            allow-clear
          />
        </a-form-item>
        <a-form-item v-if="!isfiletype" label="文件路径">
          <a-input
            :disabled="operation === 1"
            v-decorator="[
              'addr',
              {
                rules: [
                  {
                    required: true,
                    message: '文件路径不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入文件路径"
            allow-clear
          />
        </a-form-item>
        <!-- <a-form-item v-if="isfiletype" label="端口">
          <a-input-number
            :disabled="operation === 1"
            v-decorator="[
              'port',
              {
                rules: [
                  {
                    required: true,
                    message: '端口不能为空'
                  }
                ]
              }
            ]"
          />
        </a-form-item> -->
        <a-form-item v-if="isfiletype" label="数据库名称">
          <a-input
            :disabled="operation === 1"
            v-decorator="[
              'dbName',
              {
                rules: [
                  {
                    required: true,
                    message: '数据库名称不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入数据库名称"
            allow-clear
          />
        </a-form-item>
        <div>
          <a-form-item label="用户名">
           <a-input
             :disabled="operation === 1"
             v-decorator="[
               'userName',
               {
                 rules: [
                   {
                     required: isfiletype,
                     message: '用户名不能为空'
                   }
                 ]
               }
             ]"
             placeholder="请输入用户名"
             allow-clear
           />
          </a-form-item>
          <a-form-item label="密码">
            <a-input
              :disabled="operation === 1"
              v-decorator="[
                'password',
                {
                  rules: [
                    {
                      required: isfiletype,
                      message: '密码不能为空'
                    }
                  ]
                }
              ]"
              placeholder="请输入密码"
            />
          </a-form-item>
        </div>
        <a-form-item v-if="isSpatialdb" label="行政区划">
          <a-cascader
            v-decorator="['regionCode']"
            :options="options2"
            :load-data="loadData"
            placeholder="请选择地区"
            change-on-select
          />
        </a-form-item>
        <a-form-item v-if="isSpatialdb" label="时态">
          <a-date-picker
            v-decorator="['timeAttribute']"
            placeholder="请选择时态"
          />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea
            :disabled="operation === 1"
            v-decorator="['description']"
            placeholder="请输入描述"
            auto-size
          />
        </a-form-item>
        <div v-if="operation === 1">
          <a-form-item label="创建时间">
            <a-date-picker
              v-decorator="['createdTime']"
              show-time
              placeholder="请选择创建时间"
              disabled
            />
          </a-form-item>
          <a-form-item label="创建人">
            <a-input
              v-decorator="['createdBy']"
              placeholder="请输入创建人"
              allow-clear
              disabled
            />
          </a-form-item>
          <a-form-item label="修改时间">
            <a-date-picker
              v-decorator="['updatedTime']"
              show-time
              placeholder="请选择修改时间"
              disabled
            />
          </a-form-item>
          <a-form-item label="修改人">
            <a-input
              v-decorator="['updatedBy']"
              placeholder="请输入修改人"
              allow-clear
              disabled
            />
          </a-form-item>
        </div>
        <div class="btn-div">
          <button
            @click="testConnect"
            class="submit-gray"
          >
            测试连接
          </button>
          &nbsp;&nbsp;
          <button @click="submitForm" class="submit-gray">
            确定
          </button>
          &nbsp;&nbsp;
          <button @click="backToList" class="cancel-modal">{{this.operation === 1 ? "返回" : "取消"}}</button>
        </div>
      </a-form>
    </div>
      </a-tab-pane>
      <a-tab-pane key="2" tab="坐标系信息" force-render v-if="editData.isSdx === true">
        暂无数据
      </a-tab-pane>
    </a-tabs>
    <div v-if="operation === 2" class="page-container-box">
      <a-form
        :form="addOrEditForm"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 13 }"
        layout="horizontal"
      >
         <a-form-item v-show="false" label="是否为空间数据库">
          <a-switch
            v-decorator="[
              'isSdx',
              { valuePropName: 'checked', initialValue: true }
            ]"
            checked-children="是"
            un-checked-children="否"
            @change="switchChange"
          />
        </a-form-item>
        <a-form-item v-if="isSpatialdb" label="数据源分类">
          <a-cascader
            :disabled="operation === 1"
            v-decorator="[
              'catalogCode',
              {
                rules: [
                  {
                    required: true,
                    message: '数据源分类不能为空'
                  }
                ]
              }

            ]"
            :options="option3"
            placeholder="请选择数据源分类"
            change-on-select
          />
        </a-form-item>
        <a-form-item v-if="isSpatialdb" label="数据源类型">
          <a-cascader
            :disabled="operation === 1"
            v-decorator="[
              'typeCode',
              {
                rules: [
                  {
                    required: true,
                    message: '数据源类型不能为空'
                  }
                ]
              }
            ]"
            :options="option4"
            placeholder="请选择数据源类型"
            @change="onChange4"
          />
        </a-form-item>
        <a-form-item v-if="!isfiletype" label="文件路径">
          <a-input
            :disabled="operation === 1"
            v-decorator="[
              'addr',
              {
                rules: [
                  {
                    required: true,
                    message: '文件路径不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入文件路径"
            allow-clear
            style="width:79%"
          />
          <gaf-upload accept=".ubd,.udbx,.udd"  text="选择" dir="datas/" minioServiceUrl="/storage/file-storage/" @uploadComplate="uploadChange"></gaf-upload>
        </a-form-item>



        <!-- 下面是非空间数据库 -->
        <a-form-item label="数据源别名">
          <a-input
            v-decorator="[
              'dsName',
              {
                rules: [
                  {
                    required: true,
                    message: '数据源名称不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入数据源名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item v-if="!isSpatialdb" label="类型">
          <a-select
            :disabled="operation === 1"
            v-decorator="[
              'typeCode',
              {
                rules: [
                  {
                    required: true,
                    message: '请选择类型'
                  }
                ],
                initialValue: 'POSTGRESQL'
              }
            ]"
            :options="options"
            placeholder="请选择类型"
            default-active-first-option
          >
          </a-select>
        </a-form-item>
        <a-form-item v-if="isfiletype" label="服务地址">
          <a-input
            :disabled="operation === 1"
            v-decorator="[
              'addr',
              {
                rules: [
                  {
                    required: true,
                    message: '服务地址不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入服务地址(ip:端口)"
            allow-clear
          />
        </a-form-item>
        <a-form-item v-if="isfiletype" label="数据库名称">
          <a-input
            :disabled="operation === 1"
            v-decorator="[
              'dbName',
              {
                rules: [
                  {
                    required: true,
                    message: '数据库名称不能为空'
                  }
                ]
              }
            ]"
            placeholder="请输入数据库名称"
            allow-clear
          />
        </a-form-item>
        <div>
          <a-form-item label="用户名">
           <a-input
             :disabled="operation === 1"
             v-decorator="[
               'userName',
               {
                 rules: [
                   {
                     required: isfiletype,
                     message: '用户名不能为空'
                   }
                 ]
               }
             ]"
             placeholder="请输入用户名"
             allow-clear
           />
          </a-form-item>
          <a-form-item label="密码">
            <a-input-password
              :disabled="operation === 1"
              v-decorator="[
                'password',
                {
                  rules: [
                    {
                      required: isfiletype,
                      message: '密码不能为空'
                    }
                  ]
                }
              ]"
              placeholder="请输入密码"
            />
          </a-form-item>
        </div>
        <a-form-item v-if="isSpatialdb" label="行政区划">
          <a-cascader
            v-decorator="['regionCode']"
            :options="options2"
            :load-data="loadData"
            placeholder="请选择地区"
            change-on-select
          />
        </a-form-item>
        <a-form-item v-if="isSpatialdb" label="时态">
          <a-date-picker
            v-decorator="['timeAttribute']"
            placeholder="请选择时态"
          />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea
            :disabled="operation === 1"
            v-decorator="['description']"
            placeholder="请输入描述"
            auto-size
          />
        </a-form-item>
        <div v-if="operation === 1">
          <a-form-item label="创建时间">
            <a-date-picker
              v-decorator="['createdTime']"
              show-time
              placeholder="请选择创建时间"
              disabled
            />
          </a-form-item>
          <a-form-item label="创建人">
            <a-input
              v-decorator="['createdBy']"
              placeholder="请输入创建人"
              allow-clear
              disabled
            />
          </a-form-item>
          <a-form-item label="修改时间">
            <a-date-picker
              v-decorator="['updatedTime']"
              show-time
              placeholder="请选择修改时间"
              disabled
            />
          </a-form-item>
          <a-form-item label="修改人">
            <a-input
              v-decorator="['updatedBy']"
              placeholder="请输入修改人"
              allow-clear
              disabled
            />
          </a-form-item>
        </div>
        <div class="btn-div">
          <button
            @click="testConnect"
            class="submit-gray"
          >
            测试连接
          </button>
          &nbsp;&nbsp;
          <button @click="submitForm" class="submit-gray">
            确定
          </button>
          &nbsp;&nbsp;
          <button @click="backToList" class="cancel-modal">{{this.operation === 1 ? "返回" : "取消"}}</button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script>
//日期处理类库
import moment from 'moment'
export default {
  props: {
    title: {
      type: String,
      default: ''
    },
    editData: {
      type: Object,
      default: null
    },
    operation: {
      type: Number,
      default: 0
    },
    dataOfTree: {
      type: Array,
      default: () => [],
    },
    catalogCode: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      //回显时数据源分类级联选择器数据
      option3catalogCode: [],
      //回显时数据源类型级联选择器数据
      option4typeCode:[],
      //数据源类型级联选择器数据
      option4: [],
      //数据源分类级联选择器数据
      option3: this.dataOfTree,
      //判断是更新还是新增
      dataId: '',
      //行政区划数据
      options2: [],
      //非空间数据库时下拉框数据
      options: [
        { value: 'POSTGRESQL', label: 'POSTGRESQL' },
        { value: 'MYSQL', label: 'MYSQL' },
        { value: 'ORACLE', label: 'ORACLE' },
        { value: 'SQLSERVER', label: 'SQLSERVER' }
      ],
      //是否文件类型
      isfiletype: true,
       //是否数据库类型
      isSpatialdb: true,
    }
  },
  beforeMount() {
    this.addOrEditForm = this.$form.createForm(this, { name: 'addOrEditForm' })
    this.getOptions2()
    this.getOptions4()
    if (this.operation !== 2) {
       //回显时得到级联选择器路径数据  根据字典类别值和具体要得到路径的值
      this.getPath1('DataSourceClassification', this.editData.catalogCode)
      this.getPath2('DataSourceType', this.editData.typeCode)
    }
  },
  mounted() {
    //回显数据处理
    this.addOrEditForm.resetFields()
    if(this.operation === 2 && this.catalogCode !== '') {
      this.getPath1("DataSourceClassification",this.catalogCode)
      return;
    }
    const copyData = { ...this.editData }
    this.dataId = copyData.datasourceId
    delete copyData.datasourceId
    delete copyData.sortSn
    delete copyData.status
    delete copyData.tenantId
    copyData.catalogCode = this.option3catalogCode
    copyData.typeCode = this.option4typeCode
    if (this.operation !== 1) {
      delete copyData.createdTime
      delete copyData.updatedTime
      delete copyData.createdBy
      delete copyData.updatedBy
    }
    if (copyData.regionCode) {
      copyData.regionCode = copyData.regionCode.split('/')
      this.getvalue(copyData.regionCode[0])
    }
    if (this.editData.createdTime)
      this.editData.createdTime = moment(new Date(this.editData.createdTime))
    if (this.editData.updatedTime)
      this.editData.updatedTime = moment(new Date(this.editData.updatedTime))
    this.$nextTick(() => {
      this.addOrEditForm.setFieldsValue({ ...copyData })
    })
  },
  methods: {
    callback() {

    },
    moment,
    //测试链接
    testConnect() {
      this.addOrEditForm.validateFields(async err => {
        if (!err) {
          const url =
            '/sys-mgt/sys-resource-datasources/connection-param/check'
          const data = this.addOrEditForm.getFieldsValue()
          const res = await this.$axios.$post(url, {
            datasourceType: data.typeCode[1],
            host: data.addr,
            port: data.port,
            username: data.userName,
            password: data.password,
            databaseOrServiceName: data.dbName
          })
          if (res.isSuccessed) {
            this.$message.success('测试连接成功')
          } else {
            this.$message.error(`${res.message}`)
          }
        }
      })
    },
    //表单提交
    submitForm() {
      this.addOrEditForm.validateFields(async (err) => {
        if (err) {
          event.preventDefault()
          return false
        }
        let url = `/sys-mgt/sys-resource-datasources/`
        const data = this.addOrEditForm.getFieldsValue()
        if (data.regionCode) {
          data.regionCode = data.regionCode.join('/')
        }
        // data.catalogCode = data.catalogCode.join('/')
        // data.typeCode = data.typeCode.join('/')
        if (this.isSpatialdb) {
          data.catalogCode = data.catalogCode.slice(-1).join()
          data.typeCode = data.typeCode.slice(-1).join()
        }


        if (this.dataId) {
          url = url + this.dataId
          const rst = await this.$axios.put(url, data)
          if (rst.data.isSuccessed) {
            this.$message.success('更新成功')
          } else {
            this.$message.error(`更新失败,原因:${rst.data.message}`)
          }
        } else {
          const rst = await this.$axios.post(url, data)
          if (rst.data.isSuccessed) {
            this.$message.success('添加成功')
          } else {
            this.$message.error(`添加失败,原因:${rst.data.message}`)
          }
        }
        this.addOrEditForm.resetFields()
        this.$emit('submit')
      })
    },
    // 从新增修改模态框返回列表
    backToList() {
      this.addOrEditForm.resetFields()
      this.$emit('back')
    },
    //获取行政区划级联数据
    async getOptions2() {
      const url = `/sys-mgt/sys-dicts/XZQH/tree?level=1`
      const res = await this.$axios.$get(url)
      if (res.isSuccessed) {
        this.options2 = res.data
      } else {
        this.$message.error('加载数据失败,原因：' + res.message)
      }
    },
    //行政区划动态加载选项
    async loadData(selectedOptions) {
      const targetOption = selectedOptions[selectedOptions.length - 1]
      targetOption.loading = true
      const url = `/sys-mgt/sys-dicts/XZQH/tree?level=1&value=${targetOption.value}`
      const res = await this.$axios.$get(url)
      if (res.isSuccessed) {
        targetOption.loading = false
        targetOption.children = res.data
      } else {
        this.$message.error('加载数据失败,原因：' + res.message)
      }
    },
    //获取回显时行政区划级联路径
    async getvalue(targetOption) {
      const url = `/sys-mgt/sys-dicts/XZQH/tree?value=${targetOption}`
      const res = await this.$axios.$get(url)
      if (res.isSuccessed) {
        this.options2.forEach(element => {
          if (element.value === targetOption) {
            element.children = res.data
            return
          }
        });
      } else {
        this.$message.error('加载数据失败,原因：' + res.message)
      }
    },
    //获取数据源类型级联数据
    async getOptions4() {
      const url = `/sys-mgt/sys-dicts/DataSourceType/tree`
      const res = await this.$axios.$get(url)
      if (res.isSuccessed) {
        this.option4 = res.data
      } else {
        this.$message.error('加载API分组树失败,原因：' + res.message)
      }
    },
    //回显时获取数据源分类级联路径
    async getPath1(dictTypeCode, value) {
      const url = `/sys-mgt/sys-dicts/${dictTypeCode}/${value}/path`
      const res = await this.$axios.$get(url)
      if (res.isSuccessed) {
        this.option3catalogCode = res.data.map(item => item.value)
          this.addOrEditForm.setFieldsValue({
            catalogCode: this.option3catalogCode
          })
      } else {
        this.$message.error('加载路径失败,原因：' + res.message)
      }
    },
    //回显时获取数据源类型级联路径
    async getPath2(dictTypeCode, value) {
      const url = `/sys-mgt/sys-dicts/${dictTypeCode}/${value}/path`
      const res = await this.$axios.$get(url)
      if (res.isSuccessed) {
        this.option4typeCode = res.data.map(item => item.value)
        if (this.option4typeCode[0] === "file") {
          this.isfiletype = false
        } else {
          this.isfiletype = true
        }
        this.addOrEditForm.setFieldsValue({
          typeCode: this.option4typeCode
        })
      } else {
        this.$message.error('加载路径失败,原因：' + res.message)
      }
    },
    //根据数据源类型级联change事件判断是否为文件类型
    onChange4(value) {
      if (value[0] === "file") {
        this.isfiletype = false
      } else {
        this.isfiletype = true
      }
    },
    //判断是否为空间数据库
    // switchChange(checked) {
    //   this.isSpatialdb = checked
    //   this.addOrEditForm.resetFields()
    //   this.$nextTick(function() {
    //     if(checked) {
    //       this.addOrEditForm.setFieldsValue({
    //         catalogCode: this.option3catalogCode
    //       })
    //     }
    //   })
    // },
    //上传组件uploadComplate事件给文件路径赋值
    uploadChange(data) {
      this.addOrEditForm.setFieldsValue({
        addr: data.name
      })
    },
  }
}
</script>

<style lang="less" scoped>
button {
  width: 80px;
  font-size: 12px;
  cursor: pointer;
}

.page-container {
  width: 100%;
  height: 100%;
}

.page-container-box {
  height: 100%;
}

.btn-div {
  text-align: center;
  margin: 15px 0;
}
</style>
