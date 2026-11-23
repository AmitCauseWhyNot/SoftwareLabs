SELECT DISTINCT p.ProductName
FROM Products p
JOIN Suppliers s ON s.SupplierID = p.SupplierID
WHERE LOWER(s.SupplierName) LIKE '%s%';
