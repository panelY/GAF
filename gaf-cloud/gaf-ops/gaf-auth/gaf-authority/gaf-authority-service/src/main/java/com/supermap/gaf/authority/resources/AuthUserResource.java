/*
 * Copyright© 2000 - 2021 SuperMap Software Co.Ltd. All rights reserved.
 * This program are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at http://www.apache.org/licenses/LICENSE-2.0.html.
*/
package com.supermap.gaf.authority.resources;

import com.supermap.gaf.authority.commontype.AuthUser;
import com.supermap.gaf.authority.service.AuthUserService;
import com.supermap.gaf.authority.vo.AuthUserSelectVo;
import com.supermap.gaf.authority.vo.AuthUserVo;
import com.supermap.gaf.authority.vo.PasswordVo;
import com.supermap.gaf.authority.vo.TreeNode;
import com.supermap.gaf.commontypes.MessageResult;
import com.supermap.gaf.shiro.SecurityUtilsExt;
import com.supermap.gaf.shiro.commontypes.ShiroUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 用户接口
 * @date:2021/3/25
 * @author dqc
 */
@Component
@Api(value = "用户接口")
public class AuthUserResource {
    private final AuthUserService authUserService;

    public AuthUserResource(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    @ApiOperation(value = "查询用户", notes = "根据id查询用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "string", required = true)
    })
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{userId}")
    public MessageResult<AuthUser> getById(@PathParam("userId") String userId) {
        return MessageResult.data(authUserService.getById(userId)).message("查询成功").build();
    }

    @ApiOperation(value = "查询当前用户所属租户下的所有用户信息")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/tenant")
    public MessageResult<List<AuthUserVo>> getByTenantId() {
        ShiroUser shiroUser = SecurityUtilsExt.getUser();
        String tenantId = Objects.requireNonNull(shiroUser).getTenantId();
        List<AuthUser> authUser = authUserService.getByTenantId(tenantId);
        List<AuthUserVo> collect = authUser.stream().map(user -> {
            AuthUserVo authUserVo = new AuthUserVo();
            BeanUtils.copyProperties(user, authUserVo);
            return authUserVo;
        }).collect(Collectors.toList());
        return MessageResult.data(collect).message("查询成功").build();
    }
    @ApiOperation(value = "分页条件查询")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "realName", value = "模糊查询用户真实姓名", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "pageNum", value = "页码", example = "1",defaultValue = "1", allowableValues = "range[1,infinity]",paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", example = "10", defaultValue = "10",allowableValues = "range(0,infinity]", paramType = "query", dataType = "integer"),
    })
    @GET
    @Path("/tenant/list")
    public MessageResult<Map<String, Object>>  pageListByTenantId(@QueryParam("realName") String realName,
                                                              @DefaultValue("1")@QueryParam("pageNum") Integer pageNum,
                                                              @DefaultValue("10") @QueryParam("pageSize") Integer pageSize) {
        ShiroUser shiroUser = SecurityUtilsExt.getUser();
        String tenantId = Objects.requireNonNull(shiroUser).getTenantId();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        Integer offset = (pageNum - 1) * pageSize;
        Map<String, Object> result;
        result = authUserService.pageListByTenantId(tenantId,realName,pageSize,offset);
        return MessageResult.data(result).message("查询成功").build();
    }
    @ApiOperation(value = "查询当前用户的信息")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/info")
    public MessageResult<AuthUser> getInfo() {
        AuthUser authUser = authUserService.getUserInfo();
        return MessageResult.data(authUser).message("查询成功").build();
    }

    @ApiOperation(value = "分页条件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchFieldName", value = "模糊查询字段名", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "searchFieldValue", value = "模糊查询字段值", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "orderFieldName", value = "排序字段值", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "orderMethod", value = "排序方式。升序为ASC,降序为DESC。默认不排序",allowableValues="ASC,DESC", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "pageNum", value = "页码", example = "1",defaultValue = "1", allowableValues = "range[1,infinity]",paramType = "query", dataType = "integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", example = "10", defaultValue = "10",allowableValues = "range(0,infinity]", paramType = "query", dataType = "integer"),
    })
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public MessageResult<Map<String, Object>> pageList(@QueryParam("searchFieldName") String searchFieldName,
                                       @QueryParam("searchFieldValue") String searchFieldValue,
                                       @QueryParam("orderFieldName") String orderFieldName,
                                       @QueryParam("orderMethod") String orderMethod,
                                       @DefaultValue("1")@QueryParam("pageNum") Integer pageNum,
                                       @DefaultValue("10") @QueryParam("pageSize") Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        Integer offset = (pageNum - 1) * pageSize;
        AuthUserSelectVo selectVo = AuthUserSelectVo.builder()
                .searchFieldName(searchFieldName)
                .searchFieldValue(searchFieldValue)
                .orderFieldName(orderFieldName)
                .orderMethod(orderMethod)
                .offset(offset)
                .pageSize(pageSize)
                .build();
        Map<String, Object> result;
        if (searchFieldName == null & orderFieldName == null) {
            result = authUserService.pageList(selectVo);
        } else {
            result = authUserService.searchList(selectVo);
        }
        return MessageResult.data(result).message("查询成功").build();
    }

    @ApiOperation(value = "查询部门用户树", notes = "返回的节点已组织为树形结构")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/tree")
    public MessageResult<List<TreeNode>> getUserTree() {
        ShiroUser shiroUser = SecurityUtilsExt.getUser();
        String tenantId = Objects.requireNonNull(shiroUser).getTenantId();
        List<TreeNode> result = authUserService.getUserTree(tenantId);
        return MessageResult.data(result).message("查询成功").build();
    }

    @ApiOperation(value = "查询某部门下的用户", notes = "根据部门id查询该部门下的用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "departmentId", value = "部门id", paramType = "path", dataType = "string", required = true)
    })
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/list-by-department/{departmentId}")
    public MessageResult<List<AuthUser>> listUserByDepartment(@NotEmpty @PathParam("departmentId") String departmentId) {
        ShiroUser shiroUser = SecurityUtilsExt.getUser();
        String tenantId = Objects.requireNonNull(shiroUser).getTenantId();
        List<AuthUser> result = authUserService.listUserByDepartmentWithName(tenantId, departmentId);
        return MessageResult.data(result).message("查询成功").build();
    }

    @ApiOperation(value = "查询某岗位下的用户", notes = "根据岗位id查询用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "postId", value = "岗位id", paramType = "path", dataType = "string", required = true)
    })
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/list-by-post/{postId}")
    public MessageResult<List<AuthUser>> listUserByPost(@PathParam("postId") String postId) {
        List<AuthUser> data = authUserService.listUserByPost(postId);
        return MessageResult.data(data).message("查询成功").build();
    }

    @ApiOperation(value = "新增用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authUser", value = "用户", dataTypeClass = AuthUser.class, paramType = "body",required = true)
    })
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    public MessageResult<AuthUser> insertAuthUser(@Valid AuthUser authUser) {
        ShiroUser shiroUser = SecurityUtilsExt.getUser();
        String tenantId = Objects.requireNonNull(shiroUser).getTenantId();
        authUser.setTenantId(tenantId);
        authUser.setCreatedBy(shiroUser.getAuthUser().getName());
        AuthUser insertedAuthUser = authUserService.insertAuthUser(authUser);
        return MessageResult.data(insertedAuthUser).message("新增操作成功").build();
    }

    @ApiOperation(value = "启用用户", notes = "用户状态有启用和禁用。该接口将用户状态改为启动。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "string", required = true)
    })
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/active/{userId}")
    public MessageResult<AuthUser> active(@NotEmpty @PathParam("userId") String userId) {
        AuthUser user = authUserService.active(userId);
        return MessageResult.data(user).message("新增操作成功").build();
    }

    @ApiOperation(value = "禁用用户", notes = "禁用用户即删除用户，给用户的分配的角色、岗位、挂职都会被清空")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "string", required = true)
    })
    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{userId}")
    public MessageResult<AuthUser> deleteAuthUser(@PathParam("userId") String userId) {
        AuthUser authUser = authUserService.deleteAuthUser(userId);
        return MessageResult.data(authUser).message("删除操作成功").build();
    }

    @ApiOperation(value = "批量删除用户", notes = "根据id批量删除用户。注意：给用户的分配的角色、岗位、挂职都会被清空")
    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/batch")
    public MessageResult<Void> batchDelete( List<String> userIds) {
        authUserService.batchDelete(userIds);
        return MessageResult.successe(Void.class).status(200).message("批量删除操作成功").build();
    }

    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "更新用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authUser", value = "用户", paramType = "body", dataTypeClass = AuthUser.class, required = true),
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "string", required = true)
    })
    @Path("/{userId}")
    public MessageResult<AuthUser> updateAuthUser(@Valid AuthUser authUser, @PathParam("userId") String userId) {
        ShiroUser shiroUser = SecurityUtilsExt.getUser();
        String tenantId = Objects.requireNonNull(shiroUser).getTenantId();
        authUser.setUpdatedBy(shiroUser.getAuthUser().getName());
        authUser.setUserId(userId);
        authUser.setTenantId(tenantId);
        AuthUser updatedAuthUser = authUserService.updateAuthUser(authUser);
        return MessageResult.data(updatedAuthUser).message("更新操作成功").build();
    }
    @ApiOperation(value = "变更密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "passwordVo", value = "新旧密码", dataTypeClass = PasswordVo.class, paramType = "body",required = true)
    })
    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/password-change")
    public MessageResult<Void> changePassword( PasswordVo passwordVo) {
        ShiroUser shiroUser = SecurityUtilsExt.getUser();
        authUserService.changePassword(Objects.requireNonNull(shiroUser).getAuthUser(),passwordVo.getOldPassword(),passwordVo.getNewPassword());
        return MessageResult.successe(Void.class).status(200).message("变更密码成功").build();
    }

    @ApiOperation(value = "重置密码", notes = "根据用户id重置该用户的密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "path", dataType = "string", required = true)
    })
    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{userId}/reset-password")
    public MessageResult<Void> resetPassword(@PathParam("userId") String userId) {
        authUserService.resetPassword(userId);
        return MessageResult.successe(Void.class).status(200).message("重置密码成功").build();
    }
}
