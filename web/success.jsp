<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="a" uri="/struts-tags" %>
<%--
  Created by IntelliJ IDEA.
  User: alex.gu
  Date: 2018/11/15
  Time: 14:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>省份结果</title>
</head>
<body>
    日期：<s:property value="date"/><br/>
    <h2>所有省份结果</h2>
    <s:iterator value="resultStr">
        <s:property/><br/>
    </s:iterator>
    <h2>具体省份结果</h2>
    <s:iterator value="resultDetail">
        <s:property/><br/>
    </s:iterator>
    <h2>error省份编码</h2>
    <s:iterator value="errorProvinceList">
        <s:property/><br/>
    </s:iterator>

</body>
</html>