<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Lab JDBC - SQL Runner</title>
    <style>
        body { font-family: sans-serif; padding: 20px; }
        textarea { width: 100%; height: 100px; font-family: monospace; }
        .result-box { margin-top: 20px; border: 1px solid #ccc; padding: 10px; background: #f9f9f9; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        th { background-color: #f2f2f2; }
        .error { color: red; }
        .success { color: green; }
    </style>
</head>
<body>

    <h2>Виконання SQL запитів (Захищенний режим)</h2>
    
    <form action="execute" method="post">
        <label>Введіть SQL запит:</label><br>
        <textarea name="sqlQuery">${param.sqlQuery}</textarea><br><br>
        <input type="submit" value="Виконати запит">
    </form>

    <c:if test="${not empty resultMessage}">
        <div class="result-box">
            <h3 class="${isError ? 'error' : 'success'}">
                ${isError ? 'Помилка:' : 'Результат:'}
            </h3>
            <p>${resultMessage}</p>

            <c:if test="${not empty dataList}">
                <table>
                    <thead>
                        <tr>
                            <c:forEach var="colName" items="${columnNames}">
                                <th>${colName}</th>
                            </c:forEach>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${dataList}">
                            <tr>
                                <c:forEach var="cell" items="${row}">
                                    <td>${cell}</td>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:if>
        </div>
    </c:if>

</body>
</html>