const { Log, User, Device } = require('../models');
const moment = require('moment-timezone');

exports.getLogs = async (req, res) => {
    try {
        const logs = await Log.findAll({
            include: [
                { model: User, attributes: ['email'] },
                { model: Device, attributes: ['name'] }
            ],
            order: [['timestamp', 'DESC']]
        });

        console.log('Original Logs:', logs);

        const formattedLogs = logs.map(log => {
            const formattedTimestamp = moment.utc(log.timestamp).tz('Europe/Istanbul').format('DD/MM/YYYY HH:mm:ss');
            console.log(`Log ID: ${log.id}, Original: ${log.timestamp}, Formatted: ${formattedTimestamp}`);
            return {
                id: log.id,
                user: log.User.email,
                device: log.Device.name,
                action: log.action,
                timestamp: formattedTimestamp
            };
        });

        res.json(formattedLogs);
    } catch (err) {
        res.status(500).json({ message: 'Loglar getirilemedi', error: err.message });
    }
};
