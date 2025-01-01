const express = require('express');
const bodyParser = require('body-parser');
const dotenv = require('dotenv');
const cors = require('cors'); 
const { sequelize } = require('./models');
const authRoutes = require('./routes/auth');
const deviceRoutes = require('./routes/device');
const logRoutes = require('./routes/log');

dotenv.config();

const app = express();
const port = process.env.PORT || 8080;

app.use(cors()); 
app.use(bodyParser.json()); 
app.use(bodyParser.urlencoded({ extended: true }));

app.use('/auth', authRoutes);
app.use('/devices', deviceRoutes);
app.use('/logs', logRoutes);

app.get('/', (req, res) => {
  res.send(`
      <!DOCTYPE html>
      <html lang="tr">
      <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Sistem Çalışıyor</title>
          <style>
              body, html {
                  margin: 0;
                  padding: 0;
                  width: 100%;
                  height: 100%;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                  background-color: #f0f0f0;
                  font-family: 'Roboto', Arial, sans-serif;
                  overflow: hidden;
              }
              .container {
                  text-align: center;
                  padding: 20px;
                  border-radius: 10px;
                  background-color: #fff;
                  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
              }
              .container h1 {
                  color: #0000ff;
                  font-size: 48px;
                  margin-bottom: 10px;
              }
              .container p {
                  font-size: 24px;
                  color: #333;
              }
          </style>
      </head>
      <body>
          <div class="container">
              <h1>Sistem Çalışıyor</h1>
              <p>Teknik Bilimler Meslek Yüksekokulu Otomasyon Sistemi çalışmaktadır.</p>
          </div>
      </body>
      </html>
  `);
});

app.get('/test-db', async (req, res) => {
    try {
        await sequelize.authenticate();
        res.send('Veritabanı bağlantısı başarılı!');
    } catch (error) {
        res.status(500).send('Veritabanı bağlantısı başarısız: ' + error.message);
    }
});

sequelize.sync()
  .then(() => {
    app.listen(port, () => {
      console.log(`Server running on port ${port}`);
    });
  })
  .catch(err => {
    console.error('Unable to connect to the database:', err);
  });
