package com.gs.activiti;

import junit.framework.TestCase;
import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;
/**
 * Created by Administrator on 2017/10/30.
 */
public class Activiti extends TestCase{

    private ProcessEngine processEngine;

    @Override
    protected void setUp() throws Exception {
        /**
         * ProcessEngineConfiguration为Activiti流程引擎的配置器类
         */
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mysql://localhost:3306/d_activiti")
                .setJdbcUsername("root")
                .setJdbcPassword("yp1995")
                .setJdbcDriver("com.mysql.jdbc.Driver")
                // 设置是否自动更新数据库里的数据表
                /**
                 * ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE   如果没有数据表，则创建数据表，如果有，则直接使用
                 * ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE  如果没有数据表，则抛出异常，如果有，则直接使用
                 * ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP  如果没有数据表，则创建数据表，如果有，则先删除再创建表
                 */
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngine = cfg.buildProcessEngine(); // 通过配置器构建流程引擎，activiti自动到配置的数据库中创建28张表
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");
    }

    @Test
    public void testDeploy() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.createDeployment().addClasspathResource("diagrams/goods_apply.bpmn20.xml").deploy(); //  先读取流程定义文件，再完成部署
    }

    @Test
    public void testStartProcess() {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("goods_apply"); // 启动流程
    }

    @Test
    public void testListTask() {
        // 获取当前需要执行的任务，用于查询出指定用户的所有待办任务
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList =  taskService.createTaskQuery().list();
        for (Task task : taskList) {
            task.setAssignee("emp1"); // 把任务指派给指定的用户
            taskService.complete(task.getId()); // 完成任务
            System.out.println(task.getName());
        }
    }

    @Test
    public void testLeaveProcessDeploy() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.createDeployment().addClasspathResource("diagrams/leave_process.bpmn20.xml").deploy();
    }

    @Test
    public void testStartLeaveProcess() {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.startProcessInstanceByKey("leave_process");
    }

    /**
     * assignee 指派的人
     * candidate 候选人
     */
    @Test
    public void testSubmit() {
        TaskService taskService = processEngine.getTaskService();
        // taskAssinee是指只去查找指定用户的任务。
        // 根据登录的用户名去查找此用户的待办任务
        List<Task> taskList = taskService.createTaskQuery().taskAssignee("emp").list();
        for (Task task : taskList) {
            taskService.complete(task.getId()); // 用户去执行此用户下的所有任务
        }
    }

    @Test
    public void testCheck() {
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery().taskAssignee("boss").list();
        for (Task task : taskList) {
            taskService.complete(task.getId());
        }
    }
}
