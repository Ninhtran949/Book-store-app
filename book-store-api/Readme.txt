Các lệnh thao tác với API bằng Postman  ví dụ cho user, các models khác tương tự: 
Các API CRUD cho User:
GET tất cả các User: GET http://localhost:8080/user
GET một User theo ID: GET http://localhost:8080/user/:id
Tạo một User mới: POST http://localhost:8080/user
Mở body=> raw nhập body json sau:


{
  "address": "hadaha",
  "id": "0389a5910",
  "name": "hihiahi",
  "password": "1234456",
  "phoneNumber": "3895191028",
  "strUriAvatar": ""
}

Cập nhật User theo ID: PATCH http://localhost:8080/user/:id
Xóa User theo ID: DELETE http://localhost:8080/user/:id
