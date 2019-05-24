/*
rebuild - Building your business-systems freely.
Copyright (C) 2019 devezhao <zhaofang123@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package com.rebuild.web.admin.entityhub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.business.robot.Operator;
import com.rebuild.server.business.robot.OperatorFactory;
import com.rebuild.server.business.robot.OperatorType;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.metadata.MetadataSorter;
import com.rebuild.server.metadata.entityhub.DisplayType;
import com.rebuild.server.metadata.entityhub.EasyMeta;
import com.rebuild.server.metadata.entityhub.RobotTriggerConfigService;
import com.rebuild.utils.JSONUtils;
import com.rebuild.web.BasePageControll;

import cn.devezhao.commons.web.ServletUtils;
import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Field;
import cn.devezhao.persist4j.Record;
import cn.devezhao.persist4j.engine.ID;

/**
 * TODO
 * 
 * @author devezhao zhaofang123@gmail.com
 * @since 2019/05/23
 */
@Controller
@RequestMapping("/admin/robot/")
public class RobotTriggerControll extends BasePageControll {
	
	@RequestMapping("triggers")
	public ModelAndView pageList(HttpServletRequest request) throws IOException {
		ModelAndView mv = createModelAndView("/admin/entityhub/robot/trigger-list.jsp");
		return mv;
	}
	
	@RequestMapping("trigger/{id}")
	public ModelAndView pageEditor(@PathVariable String id, 
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		ID configId = ID.valueOf(id);
		Object[] config = Application.createQuery(
				"select belongEntity,operatorType from RobotTriggerConfig where configId = ?")
				.setParameter(1, configId)
				.unique();
		if (config == null) {
			response.sendError(404, "分类数据不存在");
			return null;
		}
		
		Entity sourceEntity = MetadataHelper.getEntity((String) config[0]);
		OperatorType operatorType = OperatorType.valueOf((String) config[1]);
		
		ModelAndView mv = createModelAndView("/admin/entityhub/robot/trigger-editor.jsp");
		mv.getModel().put("configId", configId);
		mv.getModel().put("sourceEntity", sourceEntity.getName());
		mv.getModel().put("sourceEntityLabel", EasyMeta.getLabel(sourceEntity));
		mv.getModel().put("operatorType", operatorType.name());
		mv.getModel().put("operatorTypeLabel", operatorType.getDisplayName());
		return mv;
	}
	
	@RequestMapping("trigger/available-operators")
	public void getAvailableOperators(HttpServletRequest request, HttpServletResponse response) throws IOException {
		OperatorType[] ts = OperatorFactory.getAvailableOperators();
		List<String[]> list = new ArrayList<String[]>();
		for (OperatorType t : ts) {
			list.add(new String[] { t.name(), t.getDisplayName() });
		}
		writeSuccess(response, list);
	}
	
	@RequestMapping("trigger/available-entities")
	public void getAvailableEntities(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String operatorType = getParameterNotNull(request, "operator");
		Operator op = OperatorFactory.createOperator(operatorType);
		
		List<String[]> list = new ArrayList<String[]>();
		for (Entity e : MetadataHelper.getEntities()) {
			if (op.isUsableSourceEntity(e.getEntityCode())) {
				list.add(new String[] { e.getName(), EasyMeta.getLabel(e) });
			}
		}
		writeSuccess(response, list);
	}
	
	@RequestMapping("trigger/save")
	public void save(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ID user = getRequestUser(request);
		JSON formJson = ServletUtils.getRequestJson(request);
		Record record = EntityHelper.parse((JSONObject) formJson, user);
		record = Application.getBean(RobotTriggerConfigService.class).createOrUpdate(record);
		writeSuccess(response, JSONUtils.toJSONObject("id", record.getPrimary()));
	}
	
	@RequestMapping("trigger/counts-slave-fields")
	public void getCountsSlaveFields(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String sourceEntity = getParameterNotNull(request, "sourceEntity");
		Entity slave = MetadataHelper.getEntity(sourceEntity);
		Entity master = slave.getMasterEntity();

		List<String[]> slaveFields = new ArrayList<String[]>();
		List<String[]> masterFields = new ArrayList<String[]>();
		for (Field field : MetadataSorter.sortFields(slave.getFields(), DisplayType.NUMBER, DisplayType.DECIMAL)) {
			slaveFields.add(new String[] { field.getName(), EasyMeta.getLabel(field) });
		}
		for (Field field : MetadataSorter.sortFields(master.getFields(), DisplayType.NUMBER, DisplayType.DECIMAL)) {
			masterFields.add(new String[] { field.getName(), EasyMeta.getLabel(field) });
		}
		
		JSON data = JSONUtils.toJSONObject(
				new String[] { "slave", "master" }, 
				new Object[] { slaveFields.toArray(new String[slaveFields.size()][]), masterFields.toArray(new String[masterFields.size()][]) });
		writeSuccess(response, data);
	}
}
