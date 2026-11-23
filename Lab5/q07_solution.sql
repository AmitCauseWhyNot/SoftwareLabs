SELECT s.SupplierName, p.ProductName
FROM Suppliers s
JOIN Products p ON p.SupplierID = s.SupplierID
ORDER BY s.SupplierName, p.ProductName;
