SELECT c.CustomerName, COALESCE(SUM(o.Quantity),0) AS TotalQuantity
FROM Customers c
LEFT JOIN Orders o ON o.CustomerID = c.CustomerID
GROUP BY c.CustomerID, c.CustomerName
ORDER BY TotalQuantity DESC, c.CustomerID;
