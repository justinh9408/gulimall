package com.atguigu.gulimall.product.entity;

import com.atguigu.common.validGroup.AddGroup;
import com.atguigu.common.validGroup.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 品牌
 * 多场景复杂校验
 * 
 * @author justin
 * @email justinh9408@gmail.com
 * @date 2020-04-21 14:58:41
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@Null(message = "新增不需品牌id", groups = {AddGroup.class})
	@NotNull(message = "更新需要品牌Id", groups = {UpdateGroup.class})
	@TableId
	private Long brandId;

	/**
	 * 品牌名
	 */
	@NotBlank(message = "名字不能为空",groups = {AddGroup.class})
	private String name;

	/**
	 * 品牌logo地址
	 */
	@NotEmpty(message = "logo不能空", groups = {AddGroup.class})
	private String logo;

	/**
	 * 介绍
	 */
	@NotEmpty(groups = {AddGroup.class})
	private String descript;

	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	private Integer showStatus;

	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "/^[a-zA-Z]$/", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;

	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 1, message = "排序号必须大于零", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
