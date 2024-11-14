-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 24, 2024 at 10:23 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `movieticketbooking`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `insertPayment` (IN `cardNum` VARCHAR(200), IN `expiryDate` VARCHAR(100), IN `cvv` VARCHAR(50), IN `book_id` INT, IN `user_id` INT, IN `amount` DOUBLE, OUT `result` BOOLEAN)   BEGIN
    DECLARE expMonth INT;
    DECLARE expYear INT;
    DECLARE currMonth INT;
    DECLARE currYear INT;

    -- Initialize result to FALSE
    SET result = FALSE;

    -- Get current month and year
    SET currMonth = MONTH(CURDATE());
    SET currYear = YEAR(CURDATE()) % 100;

    -- Extract month and year from expiryDate
    SET expMonth = CAST(SUBSTRING(expiryDate, 1, 2) AS UNSIGNED);
    SET expYear = CAST(SUBSTRING(expiryDate, 4, 2) AS UNSIGNED);

    -- Validate card number length
    IF LENGTH(cardNum) != 16 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Card number must be 16 digits';
    END IF;

    -- Validate expiry month
    IF expMonth < 1 OR expMonth > 12 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Invalid expiry month';
    END IF;

    -- Validate expiry date is in the future
    IF expYear < currYear OR (expYear = currYear AND expMonth < currMonth) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Expiry date must be in the future';
    END IF;

    -- Validate CVV length
    IF LENGTH(cvv) != 3 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: CVV must be 3 digits.';
    END IF;

    -- Insert payment record if all validations pass
    INSERT INTO payment (cardNum, expiryDate, cvv,book_id,user_id,amount) VALUES (cardNum, expiryDate, cvv,book_id,user_id,amount);
    SET result = TRUE;

END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `admin_id` int(11) NOT NULL,
  `password` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`admin_id`, `password`) VALUES
(18, 'deep9587');

-- --------------------------------------------------------

--
-- Table structure for table `bookmovie`
--

CREATE TABLE `bookmovie` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `movie_id` int(11) NOT NULL,
  `theater_id` int(11) NOT NULL,
  `show_id` int(11) NOT NULL,
  `numberOfTickets` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookmovie`
--

INSERT INTO `bookmovie` (`id`, `user_id`, `movie_id`, `theater_id`, `show_id`, `numberOfTickets`) VALUES
(2, 2, 19, 13, 99, 50),
(3, 2, 1, 1, 1, 20),
(7, 3, 19, 13, 99, 3),
(9, 3, 19, 13, 99, 4),
(10, 3, 19, 13, 99, 2),
(11, 3, 1, 1, 1, 7),
(12, 3, 1, 1, 1, 4),
(13, 3, 1, 1, 1, 4),
(14, 3, 1, 1, 1, 5),
(15, 3, 1, 1, 1, 2),
(16, 3, 1, 1, 1, 3),
(17, 3, 19, 13, 99, 5),
(19, 44, 1, 1, 1, 3),
(22, 1, 12, 1, 12, 1);

--
-- Triggers `bookmovie`
--
DELIMITER $$
CREATE TRIGGER `update_remaining_tickets_after_delete` AFTER DELETE ON `bookmovie` FOR EACH ROW BEGIN
    UPDATE showtime 
    SET remainingTickets = remainingTickets + OLD.numberOfTickets
    WHERE id = OLD.id;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `movie`
--

CREATE TABLE `movie` (
  `movie_id` int(11) NOT NULL,
  `movie_name` varchar(100) NOT NULL,
  `genre` varchar(50) NOT NULL,
  `release_date` date NOT NULL,
  `cost` double NOT NULL,
  `rating` double NOT NULL,
  `theater_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `movie`
--

INSERT INTO `movie` (`movie_id`, `movie_name`, `genre`, `release_date`, `cost`, `rating`, `theater_id`) VALUES
(1, 'intersteller', 'SciFi', '2024-12-23', 559.99, 9.3, 1),
(11, 'the wolf of wall street', 'money', '2024-08-22', 349, 8.4, 99),
(12, 'int', 'sd', '2024-09-23', 339, 5.4, 1),
(19, 'intersteller', 'scifi', '2024-12-23', 449.99, 9.7, 13);

-- --------------------------------------------------------

--
-- Table structure for table `payment`
--

CREATE TABLE `payment` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL,
  `amount` double NOT NULL,
  `cardNum` varchar(200) NOT NULL,
  `expiryDate` varchar(100) NOT NULL,
  `cvv` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payment`
--

INSERT INTO `payment` (`id`, `user_id`, `book_id`, `amount`, `cardNum`, `expiryDate`, `cvv`) VALUES
(4, 44, 19, 1679.97, '1231231231231231', '11/44', '234'),
(5, 1, 22, 339, '1234567890123456', '05/27', '123');

-- --------------------------------------------------------

--
-- Table structure for table `showtime`
--

CREATE TABLE `showtime` (
  `id` int(11) NOT NULL,
  `movie_id` int(11) NOT NULL,
  `theater_id` int(11) NOT NULL,
  `show_date` date NOT NULL,
  `show_time` time NOT NULL,
  `total_seats` int(11) NOT NULL,
  `remainingTickets` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `showtime`
--

INSERT INTO `showtime` (`id`, `movie_id`, `theater_id`, `show_date`, `show_time`, `total_seats`, `remainingTickets`) VALUES
(1, 1, 1, '2025-11-23', '12:12:12', 290, 236),
(12, 12, 1, '2024-10-24', '12:12:12', 1, 0),
(22, 11, 99, '2024-08-23', '23:23:23', 290, 290),
(23, 11, 99, '2024-08-24', '12:12:12', 90, 94),
(99, 19, 13, '2024-12-25', '23:12:12', 90, 28);

-- --------------------------------------------------------

--
-- Table structure for table `theater`
--

CREATE TABLE `theater` (
  `theater_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `location` varchar(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `theater`
--

INSERT INTO `theater` (`theater_id`, `name`, `password`, `location`) VALUES
(1, 'pvr', 'theater1234', 'nikol'),
(13, 'MIraj', 'theater1234', 'nikol'),
(19, 'Rajhans', 'deep6677', 'nikol'),
(99, 'Rajhans', 'deep9888', 'Nikol');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `user_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`user_id`, `name`, `password`) VALUES
(1, 'user', 'user1234'),
(2, 'Deeps', 'pppp0000'),
(3, 'deep', 'deep3303'),
(12, 'deep', 'deep0000'),
(44, 'ee', 'deep4444');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`admin_id`);

--
-- Indexes for table `bookmovie`
--
ALTER TABLE `bookmovie`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk6` (`movie_id`),
  ADD KEY `fk7` (`show_id`),
  ADD KEY `fk8` (`theater_id`),
  ADD KEY `fk9` (`user_id`);

--
-- Indexes for table `movie`
--
ALTER TABLE `movie`
  ADD PRIMARY KEY (`movie_id`),
  ADD KEY `fk5` (`theater_id`);

--
-- Indexes for table `payment`
--
ALTER TABLE `payment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk3` (`user_id`),
  ADD KEY `fk4` (`book_id`);

--
-- Indexes for table `showtime`
--
ALTER TABLE `showtime`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk1` (`movie_id`),
  ADD KEY `fk2` (`theater_id`);

--
-- Indexes for table `theater`
--
ALTER TABLE `theater`
  ADD PRIMARY KEY (`theater_id`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookmovie`
--
ALTER TABLE `bookmovie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `payment`
--
ALTER TABLE `payment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookmovie`
--
ALTER TABLE `bookmovie`
  ADD CONSTRAINT `fk6` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk7` FOREIGN KEY (`show_id`) REFERENCES `showtime` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk8` FOREIGN KEY (`theater_id`) REFERENCES `theater` (`theater_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk9` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `movie`
--
ALTER TABLE `movie`
  ADD CONSTRAINT `fk5` FOREIGN KEY (`theater_id`) REFERENCES `theater` (`theater_id`);

--
-- Constraints for table `payment`
--
ALTER TABLE `payment`
  ADD CONSTRAINT `fk3` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk4` FOREIGN KEY (`book_id`) REFERENCES `bookmovie` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `showtime`
--
ALTER TABLE `showtime`
  ADD CONSTRAINT `fk1` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk2` FOREIGN KEY (`theater_id`) REFERENCES `theater` (`theater_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
