SELECT c.CustomerName
FROM Customers c
WHERE EXISTS (SELECT 1
FROM Orders o
WHERE o.CustomerID = c.CustomerID
GROUP BY o.ProductID
HAVING COUNT(*) >= 2);
