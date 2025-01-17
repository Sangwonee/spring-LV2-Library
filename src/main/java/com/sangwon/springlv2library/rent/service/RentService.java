package com.sangwon.springlv2library.rent.service;

import com.sangwon.springlv2library.book.entity.Book;
import com.sangwon.springlv2library.book.repository.BookRepository;
import com.sangwon.springlv2library.exception.BusinessLogicException;
import com.sangwon.springlv2library.exception.ExceptionCode;
import com.sangwon.springlv2library.rent.dto.RentDetailsDto;
import com.sangwon.springlv2library.rent.dto.RentHistoryResponseDto;
import com.sangwon.springlv2library.rent.dto.RentResponseDto;
import com.sangwon.springlv2library.rent.dto.ReturnStatus;
import com.sangwon.springlv2library.rent.entity.Rent;
import com.sangwon.springlv2library.rent.mapper.RentMapper;
import com.sangwon.springlv2library.rent.repository.RentRepository;
import com.sangwon.springlv2library.user.entity.User;
import com.sangwon.springlv2library.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentService {

    private final RentRepository rentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RentMapper rentMapper;

    @Transactional
    public RentResponseDto rentBook(Long bookId, Long userId) {
        User user = getUserId(userId);

        if (user.getPenaltyEndDate() != null && user.getPenaltyEndDate().isAfter(LocalDateTime.now())) {
            throw new BusinessLogicException(ExceptionCode.USER_HAS_PENALTY);
        }

        Book book = getBookId(bookId);

        if (!book.isAvailable()) {
            throw new BusinessLogicException(ExceptionCode.BOOK_NOT_AVAILABLE);
        }

        Rent rent = new Rent();
        rent.setBook(book);
        rent.setUser(user);
        rent.setRentDate(LocalDateTime.now());
        rent.setReturnStatus(ReturnStatus.RENTED);
        book.setAvailable(false);

        rentRepository.save(rent);
        bookRepository.save(book);

        return rentMapper.toResponseDto(rent);
    }

    @Transactional
    public RentResponseDto returnBook(Long bookId, Long userId) {
        Rent rent = rentRepository.findByBookBookIdAndUserUserIdAndReturnStatus(bookId, userId, ReturnStatus.RENTED)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.RENT_NOT_FOUND));

        if (rent.getRentDate().plusDays(7).isBefore(LocalDateTime.now())) {
            User user = rent.getUser();
            user.setPenaltyEndDate(LocalDateTime.now().plusWeeks(2));
            userRepository.save(user);
        }

        rent.setReturnStatus(ReturnStatus.RETURNED);
        rent.setReturnDate(LocalDateTime.now());

        Book book = rent.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        return rentMapper.toResponseDto(rentRepository.save(rent));
    }

    @Transactional
    public RentHistoryResponseDto rentList(Long userId, boolean includeReturned) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        List<Rent> rents;
        if (includeReturned) {
            rents = rentRepository.findByUserUserId(userId);
        } else {
            rents = rentRepository.findByUserUserIdAndReturnStatus(userId, ReturnStatus.RENTED);
        }

        List<RentDetailsDto> rentedBooks = rentMapper.toRentDetailsDtos(rents);

        return rentMapper.toRentHistoryResponseDto(user, rentedBooks);
    }

    private Book getBookId(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.BOOK_NOT_FOUND));
    }

    private User getUserId(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));
    }
}
