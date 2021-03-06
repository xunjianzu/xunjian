package com.wp.controller.sendtask;

import com.wp.controller.base.BaseController;
import com.wp.entity.worker.Worker;
import com.wp.service.sendtask.SendTaskService;
import com.wp.service.worker.WorkerService;
import com.wp.util.*;
import com.wp.util.mail.SimpleMailSender;
import net.sf.json.JSONArray;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wp on 2017/12/17.
 */
@Controller
@RequestMapping(value="/sendtask")
public class SendTaskController extends BaseController {
   // String menuUrl = "sendtask/goSendTask.do"; //菜单地址(权限用)
    @Resource(name = "sendTaskService")
    private SendTaskService sendTaskService;
    @Resource(name = "workerService")
    private WorkerService workerService;

    /**
     * 去发送日常巡检任务
     */
    @RequestMapping(value="/goSendTask")
    public ModelAndView goSendTask() throws Exception{
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        mv.setViewName("sendtask/sendtask");
        mv.addObject("pd", pd);
        return mv;
    }
    /**
     * 去发送临时巡检任务
     */
    @RequestMapping(value="/goSendTask1")
    public ModelAndView goSendTask1() throws Exception{
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        mv.setViewName("sendtask/sendtask1");
        mv.addObject("pd", pd);
        return mv;
    }
    /**
     * 去发送维修任务
     */
    @RequestMapping(value="/goSendTask2")
    public ModelAndView goSendTask2() throws Exception{
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        mv.setViewName("sendtask/sendtask2");
        mv.addObject("pd", pd);
        return mv;
    }

    /**
     * 发送任务
     */
    @RequestMapping(value="/sendTask")
    @ResponseBody
    public ModelAndView save(PrintWriter out) throws Exception{
       // if(!Jurisdiction.buttonJurisdiction(menuUrl, "add")){return null;}
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        pd.put("send_time",  Tools.date2Str(new Date()));	//添加时间
        pd.put("mission_condition", 1);
        sendTaskService.save(pd);
        mv.addObject("msg","success");
        mv.setViewName("save_result");
        String phonenumber = pd.getString("worker_phone");   //发送短信提醒
        String Content = "您有一条新任务，请注意查收。";
        //System.out.println(Content);
        SendMessage.sendMessage(phonenumber, Content);
        return mv;
    }


//选择员工
    @RequestMapping(value="/groupchoose")
    public void goGroup(HttpServletRequest request, HttpServletResponse response) throws Exception{
        System.out.println("跳转进来了");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String myGroup = pd.getString("groupdata");
        List<PageData> list = workerService.listGroup(pd);

        System.out.println("======"+ JSONArray.fromObject(list)+"====");
        try {
            HttpHandler.send(response, list);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
//  手机
    @RequestMapping(value="/phonechoose")
    public void goPhone(HttpServletRequest request, HttpServletResponse response) throws Exception{
        System.out.println("跳转进来了");
        ModelAndView mv = this.getModelAndView();
        PageData pd = new PageData();
        pd = this.getPageData();
        String myPhone = pd.getString("workerdata");
        List<PageData> list = workerService.listPhone(pd);

        System.out.println("======"+ JSONArray.fromObject(list)+"====");
        try {
            HttpHandler.send(response, list);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value="/uploade")
    @ResponseBody
    public String uploade(HttpServletRequest request,
                          @RequestParam(value = "file", required = false) MultipartFile file) {
        String  ffile = DateUtil.getDays(), fileName = "";
        if (null != file && !file.isEmpty()) {
            String filePath = PathUtil.getClasspath() + Const.FILEPATHIMG+ ffile;		//文件上传路径
            fileName = FileUpload.fileUp(file, filePath, this.get32UUID());				//执行上传
        }
        return ffile + "/" + fileName;
    }

    //删除图片
    @RequestMapping(value="/deltp")
    public void deltp(PrintWriter out) {
        logBefore(logger, "删除图片");
        try{
            PageData pd = new PageData();
            pd = this.getPageData();
            String mission_id = pd.getString("mission_id");
            String lock_pic = pd.getString("lock_pic");													 		//图片路径
            DelAllFile.delFolder(PathUtil.getClasspath()+ Const.FILEPATHIMG + lock_pic); 	//删除图片
            pd.put("lock_pic", "");
            if(mission_id != null){
                sendTaskService.editPic(pd);														//删除数据中图片数据
            }
            out.write("success");
            out.close();
        }catch(Exception e){
            logger.error(e.toString(), e);
        }
    }



    @InitBinder
    public void initBinder(WebDataBinder binder){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
    }


    /* ===============================权限================================== */
    public Map<String, String> getHC(){
        Subject currentUser = SecurityUtils.getSubject();  //shiro管理的session
        Session session = currentUser.getSession();
        return (Map<String, String>)session.getAttribute(Const.SESSION_QX);
    }
	/* ===============================权限================================== */
}

