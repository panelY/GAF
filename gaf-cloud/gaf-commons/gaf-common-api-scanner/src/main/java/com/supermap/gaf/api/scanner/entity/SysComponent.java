/*
 * Copyright© 2000 - 2021 SuperMap Software Co.Ltd. All rights reserved.
 * This program are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at http://www.apache.org/licenses/LICENSE-2.0.html.
*/
package com.supermap.gaf.api.scanner.entity;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
* 组件
 * @date:2021/3/25
* @author zhm
*/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("组件")
public class SysComponent implements Serializable {
    @NotNull
    @ApiModelProperty("系统组件id")
    private String sysComponentId;
    @NotNull
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("英文名称")
    private String nameCn;
    @ApiModelProperty("编码")
    private String code;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("可见性")
    @JSONField(name="isStatus")
    private Boolean status = true;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("创建时间")
    private Date createdTime;
    @ApiModelProperty("创建人")
    private String createdBy;
    @ApiModelProperty("修改时间")
    private Date updatedTime;
    @ApiModelProperty("修改人")
    private String updatedBy;
}
