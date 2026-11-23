SELECT c.CustomerName, p.ProductName, o.OrderDate
FROM Orders o
JOIN Customers c ON c.CustomerID = o.CustomerID
JOIN Products p ON p.ProductID = o.ProductID
WHERE o.OrderDate >= '2024-01-01' AND o.OrderDate < '2025-01-01';
