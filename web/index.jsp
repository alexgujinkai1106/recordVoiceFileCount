<%@ taglib prefix="s" uri="/struts-tags" %>
<%--
  Created by IntelliJ IDEA.
  User: alex.gu
  Date: 2018/11/15
  Time: 10:36
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>数据查询</title>
  </head>
  <body>
  <%--<form action="countVoiceFileAction">
    <input id="txtdate" name="date" type="text" value="请输入日期">
    <input id="btnSubmit" type="submit" value="查询">
  </form>--%>

  <s:form action="countVoiceFileAction">
    <s:textfield id="txtdate" name="date" label="请输入日期"/>例:20181116
    <s:submit id="btnSubmit" value="查询"/>
  </s:form>

  <script type="text/javascript">
      $("#btnSubmit").click(function() {
          var date = $.trim($("#txtdate").val());

          if(date == "") {
              alert("请输入日期");
              return false;
          }
          return true;
      });
      $(document).ready(function() {
          <s:if test='message!=null&&!message.equals("")'>
          alert("<s:property value="message" escapeHtml="false" />");
          </s:if>
      })
  </script>
  </body>
</html>
