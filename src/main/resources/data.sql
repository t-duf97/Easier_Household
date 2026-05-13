INSERT INTO room (floor, created_at, updated_at, name, room_type)
VALUES
    (1, NOW(),NOW(), 'Bryan', 'KITCHEN');

INSERT INTO device (room_id, name, device_type, current_value, is_online, status, created_at, updated_at)
VALUES
    (1, 'Thermostat Salon', 'THERMOSTAT', '21°C', true, 'ON', NOW(), NOW()),
    (1,'Lampe Chambre', 'LIGHT', NULL, true, 'ON', NOW(), NOW());
