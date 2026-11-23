SELECT s.SupplierName, COUNT(p.ProductID) AS ProductCount
FROM Suppliers s
LEFT JOIN Products p ON p.SupplierID = s.SupplierID
GROUP BY s.SupplierID, s.SupplierName;
