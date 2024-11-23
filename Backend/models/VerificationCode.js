module.exports = (sequelize, DataTypes) => {
    const VerificationCode = sequelize.define('VerificationCode', {
        email: {
            type: DataTypes.STRING,
            allowNull: false
        },
        code: {
            type: DataTypes.STRING,
            allowNull: false
        },
        expiration: {
            type: DataTypes.DATE,
            allowNull: false
        }
    }, {
        timestamps: false
    });

    return VerificationCode;
};
