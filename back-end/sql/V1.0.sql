CREATE TABLE `port_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `port` int(5) NOT NULL COMMENT '端口',
  `password` varchar(10) NOT NULL COMMENT '密码',
  `add_time` datetime NOT NULL COMMENT '创建时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `total_flow` BIGINT(20) DEFAULT 0 COMMENT '总流量(MB)',
  `used_flow`  BIGINT(20) DEFAULT 0 COMMENT '已经使用的流量(MB)',
  `use_type` tinyint(1) DEFAULT 1 COMMENT '0:不可用 1:可用 2:无限使用',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `port_index` (`port`) USING HASH
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

/*20190501*/
alter table `port_info` add COLUMN `mark` varchar(255) NULL DEFAULT '' COMMENT '备注';

/*20190512*/
CREATE TABLE user_info (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(32) NOT NULL DEFAULT '' COMMENT '用户名',
  password VARCHAR(128) NOT NULL DEFAULT '' COMMENT '密码',
  role TINYINT(2) NOT NULL DEFAULT 1 COMMENT '用户类型 0:管理员 1:普通用户',
  last_login_time DATETIME NULL DEFAULT NULL COMMENT '上次登录时间',
  create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

ALTER TABLE port_info add COLUMN fk_user_info_name VARCHAR(32) NULL DEFAULT NULL COMMENT '外键 所属用户name';