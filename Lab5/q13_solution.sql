SELECT c.CustomerName, COUNT(*) AS OrderCount
FROM Customers c
JOIN Orders o ON o.CustomerID = c.CustomerID
GROUP BY c.CustomerID, c.CustomerName
HAVING COUNT(*) = (SELECT MAX(n)
FROM (SELECT COUNT(*) AS n
FROM Orders
GROUP BY CustomerID) counts);
