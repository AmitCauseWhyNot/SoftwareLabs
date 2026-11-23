SELECT CustomerName
FROM Customers c
WHERE EXISTS (SELECT 1
FROM Orders o
WHERE o.CustomerID = c.CustomerID);
