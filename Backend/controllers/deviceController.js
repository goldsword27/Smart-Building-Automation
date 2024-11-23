const moment = require('moment-timezone');
const { Device, Log, User } = require('../models');

function getCurrentTimeInIstanbul() {
    return moment().tz('Europe/Istanbul').format();
}

exports.getStatus = async (req, res) => {
    try {
        const devices = await Device.findAll({
            attributes: ['id', 'name', 'type', 'status', 'value', 'powerConsumption', 'temperature', 'lightLevel', 'smokeLevel']
        });

        const devicesWithConvertedValues = devices.map(device => ({
            ...device.dataValues,
            id: device.id.toString(),
            powerConsumption: device.powerConsumption !== null ? device.powerConsumption.toString() : '0W',
            temperature: device.temperature !== null ? device.temperature.toString() : '0°C',
            lightLevel: device.lightLevel !== null ? device.lightLevel.toString() : '0%',
            smokeLevel: device.smokeLevel !== null ? device.smokeLevel.toString() : '0%'
        }));

        res.json({ devices: devicesWithConvertedValues, timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Cihaz durumları getirilemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

function getActionDescription(type, status, value) {
    switch (type) {
        case 'lamp':
            return status === 'on' ? 'lamba aygıtı açıldı' : 'lamba aygıtı kapatıldı';
        case 'wall plug':
            return status === 'on' ? 'wall plug cihazı açıldı' : 'wall plug cihazı kapatıldı';
        case 'motion-temp-light sensor':
            if (status === 'motion') return 'hareket algılandı';
            if (status === 'temp') return `sıcaklık ${value} olarak ayarlandı`;
            if (status === 'light') return `ışık seviyesi ${value} olarak ayarlandı`;
            return status === 'on' ? 'hareket/sıcaklık/ışık sensörü açıldı' : 'hareket/sıcaklık/ışık sensörü kapatıldı';
        case 'door sensor':
            return status === 'open' ? 'kapı cihazı açıldı' : 'kapı cihazı kapatıldı';
        case 'smoke sensor':
            return status === 'smoke' ? 'duman algılandı' : 'duman temizlendi';
        case 'dimmer module':
            return status === 'dim' ? `dimmer ${value} olarak ayarlandı` : 'dimmer kapatıldı';
        case 'door-window sensor':
            return status === 'open' ? 'kapı/pencere açıldı' : 'kapı/pencere kapatıldı';
        case 'dual relay switch module':
            return status === 'on' ? 'çift röle anahtarı açıldı' : 'çift röle anahtarı kapatıldı';
        case 'relay switch module':
            return status === 'on' ? 'röle anahtarı açıldı' : 'röle anahtarı kapatıldı';
        case 'rgb led':
            return status === 'on' ? 'RGB LED açıldı' : 'RGB LED kapatıldı';
        case 'rgb controller module':
            return status === 'on' ? 'RGB kontrol modülü açıldı' : 'RGB kontrol modülü kapatıldı';
        case 'roller shutter switch':
            return status === 'on' ? 'panjur anahtarı açıldı' : 'panjur anahtarı kapatıldı';
        case 'jaluzi':
            return status === 'on' ? 'jaluzi açıldı' : 'jaluzi kapatıldı';
        case 'dual switch':
            return status === 'on' ? 'çift anahtar açıldı' : 'çift anahtar kapatıldı';
        case 'switch':
            return status === 'on' ? 'anahtar açıldı' : 'anahtar kapatıldı';
        case 'roller shutter module':
            return status === 'on' ? 'panjur modülü açıldı' : 'panjur modülü kapatıldı';
        default:
            return `${type} cihazı ${status === 'on' ? 'açıldı' : 'kapandı'}`;
    }
}

exports.addDevice = async (req, res) => {
    const { name, type, identifier } = req.body;

    try {
        const existingDevice = await Device.findOne({ where: { identifier } });
        if (existingDevice) {
            return res.status(400).json({ message: 'Cihaz zaten mevcut', timestamp: getCurrentTimeInIstanbul() });
        }

        const device = await Device.create({ name, type, identifier });
        res.json({ message: 'Cihaz başarıyla eklendi', device, timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Cihaz eklenemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

exports.listDevices = async (req, res) => {
    try {
        const devices = await Device.findAll();
        res.json({ devices, timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Cihazlar getirilemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

exports.controlDevice = async (req, res) => {
    const { id } = req.params;
    const { status, value, powerConsumption, temperature, lightLevel, smokeLevel } = req.body;

    try {
        const device = await Device.findByPk(id);
        if (!device) {
            return res.status(404).json({ message: 'Cihaz bulunamadı', timestamp: getCurrentTimeInIstanbul() });
        }

        // Değerlerin güncellenmesi
        device.status = status;
        if (value !== undefined) device.value = value;
        if (powerConsumption !== undefined) device.powerConsumption = parseFloat(powerConsumption);
        if (temperature !== undefined) device.temperature = parseFloat(temperature);
        if (lightLevel !== undefined) device.lightLevel = parseFloat(lightLevel);
        if (smokeLevel !== undefined) device.smokeLevel = parseFloat(smokeLevel);

        await device.save();

        const action = getActionDescription(device.type, status, value);
        await Log.create({
            userId: req.user.id,
            deviceId: device.id,
            action: action,
            timestamp: getCurrentTimeInIstanbul()
        });

        res.json({ message: 'Cihaz durumu güncellendi', device, timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Cihaz durumu güncellenemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};

exports.deleteDevice = async (req, res) => {
    const { id } = req.params;

    try {
        const device = await Device.findByPk(id);
        if (!device) {
            return res.status(404).json({ message: 'Cihaz bulunamadı', timestamp: getCurrentTimeInIstanbul() });
        }

        await device.destroy();
        res.json({ message: 'Cihaz başarıyla silindi', timestamp: getCurrentTimeInIstanbul() });
    } catch (err) {
        res.status(500).json({ message: 'Cihaz silinemedi', error: err.message, timestamp: getCurrentTimeInIstanbul() });
    }
};
