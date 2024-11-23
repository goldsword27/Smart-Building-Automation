const moment = require('moment-timezone');

module.exports = (sequelize, DataTypes) => {
    const Device = sequelize.define('Device', {
        name: {
            type: DataTypes.STRING,
            allowNull: false
        },
        type: {
            type: DataTypes.STRING,
            allowNull: false
        },
        identifier: {
            type: DataTypes.STRING,
            allowNull: false,
            unique: true
        },
        status: {
            type: DataTypes.STRING,
            defaultValue: 'offline'
        },
        value: {
            type: DataTypes.STRING,
            allowNull: true
        },
        powerConsumption: { // ÖRNEK Duvar Prizi için güç tüketimi bilgisi
            type: DataTypes.FLOAT,
            allowNull: true
        },
        temperature: { // ÖRNEK Hareket-Sıcaklık-Işık Sensörü için sıcaklık bilgisi
            type: DataTypes.FLOAT,
            allowNull: true
        },
        lightLevel: { // ÖRNEK Hareket-Sıcaklık-Işık Sensörü için ışık seviyesi
            type: DataTypes.FLOAT,
            allowNull: true
        },
        smokeLevel: { // ÖRNEK Duman Sensörü için duman yoğunluğu bilgisi
            type: DataTypes.FLOAT,
            allowNull: true
        }
    }, {
        timestamps: true,
        getterMethods: {
            createdAt() {
                return moment(this.getDataValue('createdAt')).tz('Europe/Istanbul').format('DD/MM/YYYY HH:mm:ss');
            },
            updatedAt() {
                return moment(this.getDataValue('updatedAt')).tz('Europe/Istanbul').format('DD/MM/YYYY HH:mm:ss');
            }
        }
    });

    return Device;
};
