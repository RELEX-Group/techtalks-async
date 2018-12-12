package ru.relex.techtalks.async;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@MapperScan(basePackageClasses = AsyncCoreConfig.class)
@ComponentScan(basePackageClasses = AsyncCoreConfig.class)
@Configuration
public class AsyncCoreConfig {
}
