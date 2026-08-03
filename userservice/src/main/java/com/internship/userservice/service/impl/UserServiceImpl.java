package com.internship.userservice.service.impl;

import com.internship.userservice.dao.UserDao;
import com.internship.userservice.dto.UserDTO;
import com.internship.userservice.exception.CardLimitExceededException;
import com.internship.userservice.exception.EmailAlreadyExistsException;
import com.internship.userservice.exception.UserNotFoundException;
import com.internship.userservice.mapper.UserMapper;
import com.internship.userservice.model.CardInfo;
import com.internship.userservice.model.User;
import com.internship.userservice.repository.specification.EntitySpecifications;
import com.internship.userservice.service.UserService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

  private static final int MAX_CARDS_PER_USER = 5;

  private static final String USERS_CACHE = "users";

  private final UserDao userDao;
  private final UserMapper userMapper;
  private final CacheManager cacheManager;


  public UserServiceImpl(UserDao userDao, UserMapper userMapper, CacheManager cacheManager) {
    this.userDao = userDao;
    this.userMapper = userMapper;
    this.cacheManager = cacheManager;
  }

  @Override
  @Transactional
  public UserDTO createUser(UserDTO userDTO) {
    if (userDao.existsByEmail(userDTO.getEmail())) {
      throw new EmailAlreadyExistsException(userDTO.getEmail());
    }
    User entity = userMapper.toEntity(userDTO);
    if (entity.getActive() == null) {
      entity.setActive(true);
    }
    if (entity.getCards() != null) {
      if (entity.getCards().size() > MAX_CARDS_PER_USER) {
        throw new CardLimitExceededException(entity.getId());
      }
      for (CardInfo card : entity.getCards()) {
        card.setUser(entity);
        if (card.getActive() == null) {
          card.setActive(true);
        }
      }
    }
    User savedEntity = userDao.save(entity);
    return userMapper.toDTO(savedEntity);
  }

  @Override
  @Cacheable(value = USERS_CACHE, key = "#id")
  @Transactional(readOnly = true)
  public UserDTO getUserById(Long id) {
    return userDao.findByIdWithCards(id)
        .map(userMapper::toDTO)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  @Override
  @Cacheable(value = USERS_CACHE, key = "'email:' + #email")
  @Transactional(readOnly = true)
  public UserDTO getUserByEmail(String email) {
    return userMapper.toDTO(findUserByEmail(email));
  }

  @Override
  public Long getUserIdByEmail(String email) {
    return findUserByEmail(email).getId();
  }

  private User findUserByEmail(String email) {
    User user = userDao.findByEmailJpql(email);
    if (user == null) {
      user = userDao.findByEmailNative(email);
    }
    if (user == null) {
      throw new UserNotFoundException(email);
    }
    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDTO> getUsersByIds(List<Long> ids) {
    return userDao.findByIds(ids).stream().map(userMapper::toDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserDTO> getAllUsers(String name, String surname, Pageable pageable) {
    Specification<User> specification = Specification
            .allOf(
                    EntitySpecifications.userHasName(name),
                    EntitySpecifications.userHasSurname(surname)
            );

    return userDao.findAll(specification, pageable)
            .map(userMapper::toDTO);
  }


  @Override
  @Transactional
  @Caching(put = {
          @CachePut(value = USERS_CACHE, key = "#id"),
          @CachePut(value = USERS_CACHE, key = "'email:' + #result.email", condition = "#result != null")
  })
  public UserDTO updateUser(Long id, UserDTO updated) {
    User existing = userDao.findById(id).orElseThrow(()->new UserNotFoundException(id));
    String oldEmail = existing.getEmail();
    if (userDao.existsByEmailAndIdNot(updated.getEmail(), id)) {
      throw new EmailAlreadyExistsException(updated.getEmail());
    }
    userMapper.updateEntityFromDto(updated, existing);
    UserDTO result = userMapper.toDTO(userDao.save(existing));
    evictEmailCache(oldEmail);
    return result;
  }

  @Override
  @Transactional
  @Caching(put = {
          @CachePut(value = USERS_CACHE, key = "#id"),
          @CachePut(value = USERS_CACHE, key = "'email:' + #result.email", condition = "#result != null")
  })
  public UserDTO activateUser(Long id) {
    if (!userDao.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    userDao.activateById(id);
    User user = userDao.findByIdWithCards(id).orElseThrow(() -> new UserNotFoundException(id));
    return userMapper.toDTO(user);
  }

  @Override
  @Transactional
  @Caching(put = {
          @CachePut(value = USERS_CACHE, key = "#id"),
          @CachePut(value = USERS_CACHE, key = "'email:' + #result.email", condition = "#result != null")
  })
  public UserDTO deactivateUser(Long id) {
    if (!userDao.existsById(id)) {
      throw new UserNotFoundException(id);
    }
    userDao.deactivateById(id);
    User user = userDao.findByIdWithCards(id).orElseThrow(() -> new UserNotFoundException(id));
    return userMapper.toDTO(user);
  }

  @Override
  @Transactional
  @Caching(evict = {
          @CacheEvict(value = USERS_CACHE, key = "#id"),
          @CacheEvict(value = USERS_CACHE, key = "'email:' + #result.email", condition = "#result != null")
  })
  public UserDTO deleteUser(Long id) {
    User user = userDao.findByIdWithCards(id).orElseThrow(() -> new UserNotFoundException(id));
    UserDTO userDTO = userMapper.toDTO(user);
    userDao.deleteById(id);
    return userDTO;
  }

  private void evictEmailCache(String email) {
    Cache cache = cacheManager.getCache(USERS_CACHE);
    if (cache != null && email != null) {
      cache.evict("email:" + email);
    }
  }
}
