// server.js
const express = require("express")
const bodyParser = require("body-parser")
const cors = require("cors")
const mysql = require("mysql")

const app = express()
const port = 8026

app.use(cors())
app.use(bodyParser.json())

// 数据库连接
const db = mysql.createConnection({
  max_connections: 1000,
  host: "122.51.210.27",
  port: "3306",
  user: "1",
  password: "cyh991103",
  database: "数据",
})

db.connect((err) => {
  if (err) {
    console.log(err)
  } else {
    console.log("数据库已连接")
  }
})

// 用户登录接口
app.post("/api/login", (req, res) => {

  const { username, password } = req.body;

  console.log(username,password);
  // 在数据库中查询用户名和密码是否匹配
  const query = "SELECT * FROM user_data WHERE username = ? AND password = ?";

  db.query(query, [username, password], (err, results) => {
    if (err) {
      console.error("登录时出错:", err);
      res.status(500).json({ error: "登录时出错" });
    } else {
      // 如果查询结果不为空，表示用户名和密码匹配成功，返回登录成功的响应
      if (results.length > 0) {
        res.status(200).json({ success: true, message: "登录成功" });
      } else {
        // 否则表示用户名和密码不匹配，返回登录失败的响应
        res.status(401).json({ success: false, message: "用户名或密码错误" });
      }
    }
  });
});

// 服务器监听端口
app.listen(port, () => {
  console.log(`服务器正在端口${port}上运行`)
})
