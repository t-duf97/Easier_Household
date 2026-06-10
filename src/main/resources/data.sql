-- INSERT INTO room (floor, created_at, updated_at, name, room_type)
-- VALUES
--     (1, NOW(),NOW(), 'u', 'KITCHEN');
--
-- INSERT INTO device (room_id, name, device_type, current_value, is_online, status, created_at, updated_at)
-- VALUES
--     (1, 'Thermostat Salon', 'THERMOSTAT', '21°C', true, 'ON', NOW(), NOW()),
--     (1,'Lampe Chambre', 'LIGHT', NULL, true, 'ON', NOW(), NOW());

-- INSERT INTO app_user(created_at, updated_at, email, first_name, last_name, password_hash, username)
-- VALUES (NOW(), NOW(), 'lola@zouille.com', 'lola', 'declercq', 'jsqhfsdihfisd', 'zouzouille');
--
-- --
-- UPDATE household
-- SET time_zone = 'Europe/Brussels'
-- WHERE name = 'Maizouille';
--

-- INSERT INTO household_member(JOINED_AT, ROLE, HOUSEHOLD_ID, USER_ID)
-- VALUES (NOW(), 'OWNER', 1, 2),
--         (NOW(), 'RESIDENT', 1, 1);

SELECT * FROM room;
