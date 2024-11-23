//Admin kullanıcısı oluşturma betiği
const bcrypt = require('bcryptjs');
const { User } = require('./models');

async function createAdminUser() {
    const email = 'user@gmail.com'; // Yeni admin e-posta adresi
    const password = 'saglamsifre'; // Yeni admin şifresi
    const hashedPassword = await bcrypt.hash(password, 10);

    try {
        const existingAdmin = await User.findOne({ where: { role: 'administrator' } });
        if (existingAdmin) {
            console.log('Admin user already exists');
            return;
        }

        await User.create({
            email: email,
            password: hashedPassword,
            role: 'administrator'
        });
        console.log('Admin user created');
    } catch (err) {
        console.error('Error creating admin user:', err);
    }
}

createAdminUser();
