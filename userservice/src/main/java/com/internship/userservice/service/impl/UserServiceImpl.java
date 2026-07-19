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
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

  private static final int MAX_CARDS_PER_USER = 5;

  private final UserDao userDao;
  private final UserMapper userMapper;

  public UserServiceImpl(UserDao userDao, UserMapper userMapper) {
    this.userDao = userDao;
    this.userMapper = userMapper;
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
  public Optional<UserDTO> getUserById(Long id) {
    return userDao.findByIdWithCards(id).map(userMapper::toDTO);
  }

  @Override
  public UserDTO getUserByEmail(String email) {
    User user = userDao.findByEmailJpql(email);
    if (user == null) {
      user = userDao.findByEmailNative(email);
    }
    if (user == null) {
      throw new UserNotFoundException(email);
    }
    return userMapper.toDTO(user);
  }

  @Override
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
  public UserDTO updateUser(Long id, UserDTO updated) {
    User existing = userDao.findById(id).orElseThrow(()->new UserNotFoundException(id));
    if (userDao.existsByEmailAndIdNot(updated.getEmail(), id)) {
      throw new EmailAlreadyExistsException(updated.getEmail());
    }
    existing.setName(updated.getName());
    existing.setSurname(updated.getSurname());
    existing.setBirthDate(updated.getBirthDate());
    existing.setEmail(updated.getEmail());
    if (updated.getActive() != null) {
      existing.setActive(updated.getActive());
    }
    UserDTO result = userMapper.toDTO(userDao.save(existing));
    return result;
  }

  @Override
  @Transactional
  public UserDTO activateUser(Long id) {
    User user = userDao.findByIdWithCards(id).orElseThrow(() -> new UserNotFoundException(id));
    user.setActive(true);
    return userMapper.toDTO(userDao.save(user));
  }

  @Override
  @Transactional
  public UserDTO deactivateUser(Long id) {
    User user = userDao.findByIdWithCards(id).orElseThrow(() -> new UserNotFoundException(id));
    user.setActive(false);
    return userMapper.toDTO(userDao.save(user));
  }

  @Override
  @Transactional
  public UserDTO deleteUser(Long id) {
    User user = userDao.findByIdWithCards(id).orElseThrow(() -> new UserNotFoundException(id));
    UserDTO userDTO = userMapper.toDTO(user);
    userDao.deleteById(id);
    return userDTO;
  }
}
