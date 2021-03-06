package com.reinertisa.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.reinertisa.model.Task;
import com.reinertisa.model.TaskCategory;
import com.reinertisa.model.TaskPriority;
import com.reinertisa.model.TaskStatus;
import com.reinertisa.model.User;
import com.reinertisa.repository.TaskRepository;

@Service("taskService")
public class TaskServiceImpl implements TaskService{

	private static Logger logger = Logger.getLogger(TaskServiceImpl.class);
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	private TaskRepository taskRepository;
	
	@Autowired
	UserService userService;
	
	public TaskServiceImpl() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean addTask(Task task) {
		taskRepository.save(task);
		return task.getTaskId() != 0;
	}
	
	@Override
	public boolean createTaskStatusTable(TaskStatus status) {
		taskRepository.save(status);
		return status.getStatusId() != 0;
	}

	@Override
	public boolean createTaskPriorityTable(TaskPriority priority) {
		taskRepository.save(priority);
		return priority.getPriorityId() != 0;
	}

	@Override
	public boolean createTaskCategoryTable(TaskCategory category) {
		taskRepository.save(category);
		return category.getCategoryId() != 0;
	}

	@Override
	public String addTask(HttpServletRequest req) {
		
		Gson gson = new Gson();
		gson = new GsonBuilder().create();
		JsonObject params = new JsonObject();
		String json;
		try {
			JsonParser jsonParser = new JsonParser();
			JsonElement root = jsonParser.parse(new InputStreamReader((InputStream) req.getInputStream()));
			JsonObject rootobj = root.getAsJsonObject();
			
			
			String taskName = rootobj.get("taskName").getAsString();
			String category = rootobj.get("taskCategory").getAsString();
			TaskCategory taskCategory = getTaskCategory(category);
			
			String status = rootobj.get("taskStatus").getAsString();
			TaskStatus taskStatus = getTaskStatus(status);
			
			String description = rootobj.get("description").getAsString();
			String priority = rootobj.get("taskPriority").getAsString();
			TaskPriority taskPriority = getTaskPriority(priority);
			
			String employee = rootobj.get("employee").getAsString();
			User emp = userService.getUserByUsername(employee);
			
			String manager = rootobj.get("manager").getAsString();
			User man = userService.getUserByUsername(manager);
			
			LocalDateTime submittedDate = LocalDateTime.now();
			String dueDate = rootobj.get("taskDueDate").getAsString();
			
			
			Task newTask = new Task(taskName, taskCategory, taskStatus, description, taskPriority, emp, null, man, submittedDate, LocalDateTime.parse(dueDate, formatter));
			
			int taskId = taskRepository.save(newTask);
			
			if (taskId > 0) {
				params = getParams(newTask);
//				params.addProperty("taskId", taskId);
//				params.addProperty("taskName", newTask.getTaskName());
//				params.addProperty("taskCategory", newTask.getTaskCategory().getCategory());
//				params.addProperty("taskStatus", newTask.getTaskStatus().getStatus());
//				params.addProperty("description", newTask.getDescription());
//				params.addProperty("taskPriority", newTask.getTaskPriority().getPriority());
//				params.addProperty("employee", emp.getUsername());
//				
//				params.addProperty("taskCompletedDate", "null");
//				params.addProperty("manager", man.getUsername());
//				params.addProperty("taskSubmittedDate", newTask.getTaskSubmittedDate().format(formatter));
//				params.addProperty("taskDueDate", newTask.getTaskDueDate().format(formatter));
//				
			} else {
				System.out.println(taskId);
				params.addProperty("status", "process failed in try block");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			params.addProperty("status", "process failed in catch block");
			
		}
		
		return gson.toJson(params);
	}

	@Override
	public String updateTask(HttpServletRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String assignTask(HttpServletRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAllTasks() {

		Gson gson = new Gson();
		gson = new GsonBuilder().create();
		JsonObject params = new JsonObject();
		String json = null;
		try {
			List<Task> tasks = taskRepository.findAll();
			
			if (tasks == null || tasks.size() == 0) {
				params.addProperty("status", "no record");
			} else {
				JsonArray jobj = new JsonArray();
				
				for (Task t: tasks) {					
					jobj.add(getParams(t));
				}
				
				json = jobj.toString();
			}
			
		} catch (Exception e) {
			
		}
		return json;
	}

	

	@Override
	public String getTask(HttpServletRequest req) {
		// TODO Auto-generated method stub
		return null;
	}	
	
	private JsonObject getParams(Task t) {
		JsonObject params = new JsonObject();
		
		params.addProperty("taskId", t.getTaskId());
		params.addProperty("taskName", t.getTaskName());
		params.addProperty("taskCategory", t.getTaskCategory().getCategory());
		params.addProperty("taskStatus", t.getTaskStatus().getStatus());
		params.addProperty("description", t.getDescription());
		params.addProperty("taskPriority", t.getTaskPriority().getPriority());
		if (t.getEmployee() != null) {
			params.addProperty("employee", t.getEmployee().getUsername());
		} else {
			params.addProperty("employee", "null");
		}
		params.addProperty("taskCompletedDate", "null");
		params.addProperty("manager", t.getManager().getUsername());
		params.addProperty("taskSubmittedDate", t.getTaskSubmittedDate().format(formatter));
		if (t.getTaskDueDate() != null ) {
			params.addProperty("taskDueDate", t.getTaskDueDate().format(formatter));
		} else {
			params.addProperty("taskDueDate", "null");
		}
		
		return params;
	}
	
	private TaskCategory getTaskCategory(String category) {
		TaskCategory ret = null;
		
		if (category.equals("Planning")) {
			ret = new TaskCategory(1, category);
		} else if (category.equals("Analysis")) {
			ret = new TaskCategory(2, category);
		} else if (category.equals("Design")) {
			ret = new TaskCategory(3, category);
		} else if (category.equals("Development")) {
			ret = new TaskCategory(4, category);
		} else if (category.equals("Testing")) {
			ret = new TaskCategory(5, category);
		} else if (category.equals("Implementation")) {
			ret = new TaskCategory(6, category);
		}
		
		return ret;
	}
	
	private TaskStatus getTaskStatus(String status) {
		TaskStatus ret = null;
		if (status.equals("Pending")) {
			ret = new TaskStatus(1, status);
		} else if (status.equals("In Progress")) {
			ret = new TaskStatus(2, status);
		} else if (status.equals("In Test")) {
			ret = new TaskStatus(3, status);
		}
		return ret;
	}
	
	private TaskPriority getTaskPriority(String priority) {
		TaskPriority ret = null;
		
		if (priority.equals("Do Now")) {
			ret = new TaskPriority(1, priority);
		} else if (priority.equals("Do Next")) {
			ret = new TaskPriority(2, priority);
		} else if (priority.equals("Do Last")) {
			ret = new TaskPriority(3, priority);
		} else if (priority.equals("Do Never")) {
			ret = new TaskPriority(4, priority);
		}
		return ret;
	}

}
