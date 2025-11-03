package com.ntd.uniorien.config;

import com.ntd.uniorien.constant.PredefinedRole;
import com.ntd.uniorien.entity.Role;
import com.ntd.uniorien.entity.User;
import com.ntd.uniorien.repository.RoleRepository;
import com.ntd.uniorien.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @NonFinal
    @Value("${app.admin.password}")
    String adminPassword;

    @NonFinal
    @Value("${app.admin.email}")
    String adminEmail;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findUserByEmail(adminEmail).isEmpty()) {

                // crate roles
                roleRepository.save(Role.builder()
                        .roleName(PredefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .roleName(PredefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());


                User admin = User.builder()
                        .role(adminRole)
                        .email(adminEmail)
                        .fullName("System Admin")
                        .password(passwordEncoder.encode(adminPassword))
                        .build();

                userRepository.save(admin);
                log.info("Created admin user");
            }
//            List<String> majorGroups = List.of(
//                    "Kế toán - Kiểm toán",
//                    "Tài chính - Ngân hàng - Bảo hiểm",
//                    "Kinh tế - Quản trị kinh doanh - Thương Mại",
//                    "Công nghệ thông tin - Tin học",
//                    "Công nghiệp bán dẫn",
//                    "Báo chí - Marketing - Quảng cáo - PR",
//                    "Sư phạm - Giáo dục",
//                    "Y - Dược",
//                    "Bác sĩ thú y",
//                    "Công an - Quân đội",
//                    "Thiết kế đồ họa - Game - Đa phương tiện",
//                    "Xây dựng - Kiến trúc - Giao thông",
//                    "Ngoại giao - Ngoại ngữ",
//                    "Ngoại thương - Xuất nhập khẩu - Kinh tế quốc tế",
//                    "Du lịch - Khách sạn",
//                    "Ô tô - Cơ khí - Chế tạo",
//                    "Điện lạnh - Điện tử - Điện - Tự động hóa",
//                    "Hàng hải - Thủy lợi - Thời tiết",
//                    "Hàng không - Vũ trụ - Hạt nhân",
//                    "Công nghệ vật liệu",
//                    "Công nghệ chế biến thực phẩm",
//                    "Công nghệ In - Giấy",
//                    "Công nghệ sinh - Hóa",
//                    "Luật - Tòa án",
//                    "Mỏ - Địa chất",
//                    "Mỹ thuật - Âm nhạc - Nghệ thuật",
//                    "Tài nguyên - Môi trường",
//                    "Tâm lý",
//                    "Thể dục - Thể thao",
//                    "Thời trang - May mặc",
//                    "Thủy sản - Lâm nghiệp - Nông nghiệp",
//                    "Toán học và thống kê",
//                    "Nhân sự - Hành chính",
//                    "Văn hóa - Chính trị - Khoa học xã hội",
//                    "Khoa học tự nhiên khác"
//            );
//
//            majorGroups.forEach(majorGroup -> {
//                MajorGroup mg = MajorGroup.builder()
//                        .name(majorGroup)
//                        .build();
//                majorGroupRepository.save(mg);
//            });



        };
    }
}
