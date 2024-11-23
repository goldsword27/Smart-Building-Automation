module.exports = (sequelize, DataTypes) => {
    const Log = sequelize.define('Log', {
        userId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        deviceId: {
            type: DataTypes.INTEGER,
            allowNull: false
        },
        action: {
            type: DataTypes.STRING,
            allowNull: false
        },
        timestamp: {
            type: DataTypes.DATE,
            allowNull: false,
            defaultValue: DataTypes.NOW
        }
    });

    Log.associate = models => {
        Log.belongsTo(models.User, { foreignKey: 'userId' });
        Log.belongsTo(models.Device, { foreignKey: 'deviceId' });
    };

    return Log;
};
