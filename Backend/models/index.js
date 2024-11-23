const { Sequelize, DataTypes } = require('sequelize');
require('dotenv').config();

const sequelize = new Sequelize(process.env.DB_NAME, process.env.DB_USER, process.env.DB_PASSWORD, {
    host: process.env.DB_HOST,
    port: process.env.DB_PORT,
    dialect: 'mysql',
    dialectOptions: {
        connectTimeout: 60000 
    },
    logging: console.log
});

const User = require('./User')(sequelize, DataTypes);
const VerificationCode = require('./VerificationCode')(sequelize, DataTypes);
const Device = require('./Device')(sequelize, DataTypes);
const Log = require('./Log')(sequelize, DataTypes);

User.hasMany(Log, { foreignKey: 'userId' });
Device.hasMany(Log, { foreignKey: 'deviceId' });
Log.belongsTo(User, { foreignKey: 'userId' });
Log.belongsTo(Device, { foreignKey: 'deviceId' });

const db = {};
db.Sequelize = Sequelize;
db.sequelize = sequelize;
db.User = User;
db.VerificationCode = VerificationCode;
db.Device = Device;
db.Log = Log;

module.exports = db;
