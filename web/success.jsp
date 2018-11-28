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
    <%--<script src="jquery-3.3.1.min.js"/>--%>
</head>
<body>
    日期：<s:property value="date"/><br/>
    <h2>所有省份结果</h2>
    一共查询<s:property value="resultStr"/>个省份。<br/>
    <s:iterator value="preResult">
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

    <%--&lt;%&ndash;javascript入口&ndash;%&gt;
    <script>
        window.onload = function () {
            
        }
    </script>

    &lt;%&ndash;jquery入口&ndash;%&gt;
    <script type="text/javascript">
        $(function () {

        })
    </script>--%>
</body>
</html>