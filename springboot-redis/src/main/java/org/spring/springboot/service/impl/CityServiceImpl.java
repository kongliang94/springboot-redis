package org.spring.springboot.service.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.springboot.dao.CityDao;
import org.spring.springboot.domain.City;
import org.spring.springboot.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

/**
 * 
 *
 */
@Service
public class CityServiceImpl implements CityService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CityServiceImpl.class);

    @Autowired
    private CityDao cityDao;
    //注入redis的
    @Autowired
    private RedisTemplate redisTemplate;

    public City findCityByName(String cityName) {
        return cityDao.findByName(cityName);
    }
    /**
     * 获取城市逻辑：
     * 如果缓存存在，从缓存中获取城市信息
     * 如果缓存不存在，从 DB 中获取城市信息，然后插入缓存
     */

	@Override
	public City findCityById(Long id) {
		 //从缓存中获取城市信息
        String key = "city_" + id;
        ValueOperations<String, City> operations = redisTemplate.opsForValue();
        //缓存存在
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
        	City city = operations.get(key);
            LOGGER.info("CityServiceImpl.findCityById() : 从缓存中获取了城市 >> " + city.toString());
            return city;
		}
        //缓存不存在，将数据存入缓存
        City city = cityDao.findById(id);
        //插入缓存操作
        operations.set(key, city);
        //插入的数据设置10秒刷新
        //operations.set(key, city, 10, TimeUnit.SECONDS);
        LOGGER.info("CityServiceImpl.findCityById() : 城市插入缓存 >> " + city.toString());
		return city;
	}

	@Override
	public Long saveCity(City city) {
		 return cityDao.saveCity(city);
	}
	
	/**
     * 更新城市逻辑：
     * 如果缓存存在，删除
     * 如果缓存不存在，不操作
     */
	@Override
	public Long updateCity(City city) {
		Long con=cityDao.updateCity(city);
		
		String key = "city_" + city.getId();
		boolean haskey=redisTemplate.hasKey(key);
		if (haskey) {
			redisTemplate.delete(key);
			LOGGER.info("CityServiceImpl.updateCity() : 从缓存中删除城市 >> " + city.toString());
		}
		return con;
	}

	@Override
	public Long deleteCity(Long id) {
		Long ret = cityDao.deleteCity(id);

		// 缓存存在，删除缓存
		String key = "city_" + id;
		boolean hasKey = redisTemplate.hasKey(key);
		if (hasKey) {
			redisTemplate.delete(key);

			LOGGER.info("CityServiceImpl.deleteCity() : 从缓存中删除城市 ID >> " + id);
		}
		return ret;
	}

}
