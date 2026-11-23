SELECT p.ProductName
FROM Products p
JOIN Orders o ON o.ProductID = p.ProductID
GROUP BY p.ProductID, p.ProductName
HAVING SUM(o.Quantity) > 4;
