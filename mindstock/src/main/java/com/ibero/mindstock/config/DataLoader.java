package com.ibero.mindstock.config;

import com.ibero.mindstock.model.*;
import com.ibero.mindstock.model.enums.*;
import com.ibero.mindstock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LabRepository labRepository;
    private final ItemRepository itemRepository;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) return;

        System.out.println("🧠 MindStock: Cargando datos iniciales...");

        // --- USUARIOS DEMO ---
        // Password para todos los demos: "mindstock2026"
        String demoPassword = BCrypt.hashpw("mindstock2026", BCrypt.gensalt());

        User laboratorista = User.builder()
                .nombre("Roberto").apellido("Hernández")
                .email("roberto.hernandez@ibero.mx")
                .numEstudiante("LAB001")
                .rol(Rol.LABORATORISTA)
                .passwordHash(demoPassword)
                .rfidUid("LAB-RFID-001")
                .build();

        User admin = User.builder()
                .nombre("María").apellido("González")
                .email("maria.gonzalez@ibero.mx")
                .numEstudiante("ADM001")
                .rol(Rol.ADMIN)
                .passwordHash(demoPassword)
                .rfidUid("ADM-RFID-001")
                .build();

        User alumno1 = User.builder()
                .nombre("Carlos").apellido("Méndez")
                .email("carlos.mendez@ibero.mx")
                .numEstudiante("ALU20230001")
                .rol(Rol.ALUMNO)
                .passwordHash(demoPassword)
                .build();

        User alumno2 = User.builder()
                .nombre("Ana").apellido("García")
                .email("ana.garcia@ibero.mx")
                .numEstudiante("ALU20230002")
                .rol(Rol.ALUMNO)
                .passwordHash(demoPassword)
                .build();

        User alumno3 = User.builder()
                .nombre("Luis").apellido("Torres")
                .email("luis.torres@ibero.mx")
                .numEstudiante("ALU20230003")
                .rol(Rol.ALUMNO)
                .passwordHash(demoPassword)
                .build();

        userRepository.save(laboratorista);
        userRepository.save(admin);
        userRepository.save(alumno1);
        userRepository.save(alumno2);
        userRepository.save(alumno3);

        // --- CATEGORÍAS ---
        Category electronica = categoryRepository.save(
                Category.builder().nombre("Electrónica")
                        .descripcion("Componentes y equipos electrónicos").build());
        Category herramientas = categoryRepository.save(
                Category.builder().nombre("Herramientas")
                        .descripcion("Herramientas de fabricación y reparación").build());
        Category mecanica = categoryRepository.save(
                Category.builder().nombre("Mecánica")
                        .descripcion("Instrumentos de medición mecánica").build());
        Category robotica = categoryRepository.save(
                Category.builder().nombre("Robótica")
                        .descripcion("Plataformas y kits de robótica").build());
        Category computacion = categoryRepository.save(
                Category.builder().nombre("Computación")
                        .descripcion("Computadoras de placa única").build());
        Category soldadura = categoryRepository.save(
                Category.builder().nombre("Soldadura")
                        .descripcion("Materiales para soldadura electrónica").build());

        // --- LABORATORIOS ---
        Lab labA = labRepository.save(Lab.builder()
                .nombre("Lab A-204").ubicacion("Edificio A, Piso 2")
                .responsable("Roberto Hernández").build());
        Lab labB = labRepository.save(Lab.builder()
                .nombre("Lab B-101").ubicacion("Edificio B, Piso 1")
                .responsable("Roberto Hernández").build());
        Lab labRobotica = labRepository.save(Lab.builder()
                .nombre("Lab Robótica").ubicacion("Edificio D, Piso 1")
                .responsable("María González").build());

        // --- ITEMS (los 12 objetos REALES del modelo entrenado) ---
        // IMPORTANTE: Los IDs deben coincidir con LABEL_TO_ITEM_ID en detector.py
        itemRepository.save(Item.builder()
                .nombre("Arduino Mega 2560")
                .descripcion("Microcontrolador ATmega2560, 54 pines digitales I/O")
                .category(electronica).lab(labB)
                .cantidadTotal(8).cantidadDisponible(8)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Arduino Uno Q")
                .descripcion("Arduino Uno R3 - microcontrolador ATmega328P")
                .category(electronica).lab(labB)
                .cantidadTotal(12).cantidadDisponible(12)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Dremel Multiherramienta")
                .descripcion("Dremel rotativa multiusos para corte y pulido")
                .category(herramientas).lab(labA)
                .cantidadTotal(3).cantidadDisponible(3)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Flux para Soldadura")
                .descripcion("Flux pasta para soldadura electrónica")
                .category(soldadura).lab(labA)
                .cantidadTotal(15).cantidadDisponible(15)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Kit de LEDs")
                .descripcion("Kit surtido de LEDs de varios colores y tamaños")
                .category(electronica).lab(labB)
                .cantidadTotal(20).cantidadDisponible(20)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Kit de Resistencias 1/4W")
                .descripcion("Kit surtido de 600 resistencias de carbón")
                .category(electronica).lab(labA)
                .cantidadTotal(15).cantidadDisponible(15)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Multímetro Grande Fluke")
                .descripcion("Multímetro digital de banco profesional")
                .category(electronica).lab(labA)
                .cantidadTotal(4).cantidadDisponible(4)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Multímetro Pequeño Portátil")
                .descripcion("Multímetro digital portátil de bolsillo")
                .category(electronica).lab(labA)
                .cantidadTotal(8).cantidadDisponible(8)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Raspberry Pi 5")
                .descripcion("Computadora de placa única con 8GB RAM, WiFi, BT")
                .category(computacion).lab(labB)
                .cantidadTotal(6).cantidadDisponible(6)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("ROSMaster X3")
                .descripcion("Robot móvil con ruedas mecanum, plataforma ROS")
                .category(robotica).lab(labRobotica)
                .cantidadTotal(2).cantidadDisponible(2)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Taladro DeWalt 20V")
                .descripcion("Taladro inalámbrico DeWalt 20V con batería")
                .category(herramientas).lab(labA)
                .cantidadTotal(3).cantidadDisponible(3)
                .status(ItemStatus.DISPONIBLE).build());

        itemRepository.save(Item.builder()
                .nombre("Vernier Digital 150mm")
                .descripcion("Calibrador vernier digital de acero inoxidable")
                .category(mecanica).lab(labA)
                .cantidadTotal(10).cantidadDisponible(10)
                .status(ItemStatus.DISPONIBLE).build());

        System.out.println("✅ MindStock: Datos iniciales cargados");
        System.out.println("📊 " + userRepository.count() + " usuarios | " +
                itemRepository.count() + " items");
        System.out.println("🔑 Password demo (todos los usuarios): mindstock2026");
    }
}