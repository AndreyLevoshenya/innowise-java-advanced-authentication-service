# AUTHENTICATION SERVICE

1) Create two roles: admin and user. Admin should have an access to all endpoints. Admin could add, view, delete and update items, 
view and activate/deactivate users, view all orders, all cards, and all payments. 
User should have an access only to his resources (user info, cards, orders and payments in next tasks)

2) Add endpoints: create token, validate token, refresh token and save user credentials

3) Use login/password for Authentication (Login endpoint should return JWT tokens: access and refresh)

4) Store login/password in additional DB (store hashed passwords using BCrypt with unique salt for each password)

5) Use JWT to implement Authorization for all endpoints in User Service

6) JWT token should contain user id and role. Get user id from token when you need it in specific endpoints for user role

7) Catch all security exceptions

8) Create Docker file of the service

9) OPTIONAL! Implement logic above using KeyCloack