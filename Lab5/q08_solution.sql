SELECT c.*
FROM Customers c
WHERE EXISTS (SELECT 1
FROM Orders o
JOIN Products p ON p.ProductID = o.ProductID
JOIN Suppliers s ON s.SupplierID = p.SupplierID
WHERE o.CustomerID = c.CustomerID AND s.City = c.City);
