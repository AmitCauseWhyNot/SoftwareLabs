SELECT City, SUM(CustomerCount) AS CustomerCount, SUM(SupplierCount) AS SupplierCount
FROM (SELECT City, 1 AS CustomerCount, 0 AS SupplierCount
FROM Customers
UNION ALL SELECT City, 0, 1
FROM Suppliers) locations
GROUP BY City
ORDER BY City;
