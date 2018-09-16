package com.web.site;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 票据控制器
 *
 * @author Egan
 * @date 2018/9/8 19:10
 **/
@Controller
@RequestMapping("ticket")
public class TicketController {

    private Logger log = LogManager.getLogger();

    private volatile long TICKET_ID_SEQUENCE = 1;

    private Map<Long, Ticket> ticketDatabase = new LinkedHashMap<>();

    /**
     * 列出所有票据
     *
     * @date 2018/9/8 19:29
     * @param model  通用模型
     * @return java.lang.String url
     **/
    @RequestMapping(value = {"", "list"}, method = RequestMethod.GET)
    public String list(Map<String, Object> model){
        log.debug("Listing tickets.");
        model.put("ticketDatabase", ticketDatabase);
        return "ticket/list";
    }

    /**
     * 查看特定票据
     *
     * @date 2018/9/8 19:30
     * @param model 通用模型
	 * @param ticketId 目标票据id
     * @return org.springframework.web.servlet.ModelAndView
     **/
    @RequestMapping(value = "view/{ticketId}", method = RequestMethod.GET)
    public ModelAndView view(Map<String, Object> model,
                             @PathVariable("ticketId") long ticketId){
        Ticket ticket = ticketDatabase.get(ticketId);
        if(ticket == null)
            return this.getListRedirectModelAndView();
        model.put("ticketId", Long.toString(ticketId));
        model.put("ticket", ticket);
        return new ModelAndView("ticket/view");
    }

    /**
     * 下载特定票据的附件
     *
     * @date 2018/9/8 19:50
     * @param ticketId 目标票据id
	 * @param name 附件名称
     * @return org.springframework.web.servlet.View
     **/
    @RequestMapping(value = "{ticketId}/{attachment:.+}", method = RequestMethod.GET)
    public View download(@PathVariable("ticketId") long ticketId,
                         @PathVariable("attachment") String name){

        Ticket ticket = ticketDatabase.get(ticketId);
        if(ticket == null){
            return this.getListRedirectView();
        }

        Attachment attachment = ticket.getAttachment(name);
        if(attachment == null){
            log.info("Couldn't found request attachment {} on ticket {}", name, ticket);
            return this.getListRedirectView();
        }

        return new DownloadView(attachment.getName(), attachment.getMimeContentType(), attachment.getContent());
    }

    /**
     * 创建票据视图
     *
     * @date 2018/9/8 19:58
     * @param model   通用模型
     * @return java.lang.String
     **/
    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String create(Map<String, Object> model){
        model.put("ticketForm", new Form());
        return "ticket/add";
    }

    /**
     * 创建票据
     *
     * @date 2018/9/8 19:58
     * @param session 会话
	 * @param form   提交表单
     * @return org.springframework.web.servlet.View
     **/
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public View create(HttpSession session, Form form) throws IOException {

        Ticket ticket = new Ticket();
        ticket.setId(this.getNextTicketId());
        ticket.setCustomerName((String) session.getAttribute("username"));
        ticket.setBody(form.getBody());
        ticket.setSubject(form.getSubject());
        ticket.setDateCreated(Instant.now());

        for(MultipartFile filePart : form.getAttachments()){
            log.debug("Processing attachments for new ticket");
            Attachment attachment = new Attachment();
            attachment.setName(filePart.getOriginalFilename());
            attachment.setMimeContentType(filePart.getContentType());
            attachment.setContent(filePart.getBytes());

            if((attachment.getName() != null && attachment.getName().length() > 0) ||
                    (attachment.getContent() != null && attachment.getContent().length > 0))
                ticket.addAttachment(attachment);
        }

        ticketDatabase.put(ticket.getId(), ticket);

        return new RedirectView("/ticket/view/" + ticket.getId(), true, false);

    }

    private ModelAndView getListRedirectModelAndView(){return new ModelAndView(this.getListRedirectView());}

    private View getListRedirectView(){return new RedirectView("/ticket/list", true, false);}

    private synchronized long getNextTicketId(){return this.TICKET_ID_SEQUENCE++; }

    public static class Form{

        private String subject;

        private String body;

        private List<MultipartFile> attachments;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public List<MultipartFile> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<MultipartFile> attachments) {
            this.attachments = attachments;
        }
    }
}