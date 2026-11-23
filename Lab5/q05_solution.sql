SELECT ProductName
FROM Products
WHERE StockQuantity > 0 AND Price = (SELECT MAX(Price)
FROM Products
WHERE StockQuantity > 0);
