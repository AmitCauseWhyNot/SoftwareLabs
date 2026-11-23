SELECT s.SupplierName
FROM Suppliers s
WHERE EXISTS (SELECT 1
FROM Products p
WHERE p.SupplierID = s.SupplierID AND p.Price > 1000);
