import api from "@/app/api/axios";

export interface BookingDTO {
  id: number;
  flightId: number;
  passengerId?: number;
  seatNumber: string;
  bookingStatus: "CONFIRMED" | "CANCELLED" | "WAITLISTED";
  price: number;
}

export interface BookingCreateRequest {
  flightId: number;
  seatNumber: string;
  passengerId?: number;
}

export interface BookingResponse {
  bookingId: number;
  status: "CONFIRMED" | "CANCELLED" | "WAITLISTED";
  finalPrice: number;
  message: string;
}

export interface PassengerBookingDTO {
  bookingId: number;
  flightNumber: string;
  origin: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  bookingStatus: string;
  seatNumber: string;
  price: number;
  loyaltyEarned: number;
}

export interface BookingAdminDTO {
  id: number;
  flightNumber: string;
  origin: string;
  destination: string;
  passengerName: string;
  seatNumber: string;
  bookingStatus: "CONFIRMED" | "CANCELLED" | "WAITLISTED";
  price: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const getAdminBookings = async (
  page = 0,
  size = 50
): Promise<PagedResponse<BookingAdminDTO>> => {
  const res = await api.get<PagedResponse<BookingAdminDTO>>(
    `/bookings?page=${page}&size=${size}`
  );
  return res.data;
};

export const getBookings = async (
  page = 0,
  size = 50
): Promise<PagedResponse<BookingDTO>> => {
  const res = await api.get<PagedResponse<BookingDTO>>(
    `/bookings?page=${page}&size=${size}`
  );
  return res.data;
};

export const getBookingById = async (id: number): Promise<BookingDTO> => {
  const res = await api.get<BookingDTO>(`/bookings/${id}`);
  return res.data;
};

export const createBooking = async (
  data: BookingCreateRequest
): Promise<BookingResponse> => {
  const res = await api.post<BookingResponse>("/bookings", data);
  return res.data;
};

export const cancelBooking = async (id: number): Promise<void> => {
  await api.post(`/bookings/${id}/cancel`);
};

export const updateBooking = async (
  id: number,
  data: Partial<BookingDTO>
): Promise<BookingDTO> => {
  const res = await api.put<BookingDTO>(`/bookings/${id}`, data);
  return res.data;
};

export const getPassengerBookings = async (
  passengerId: number
): Promise<PassengerBookingDTO[]> => {
  const res = await api.get<PassengerBookingDTO[]>(
    `/passengers/${passengerId}/bookings`
  );
  return res.data;
};

export const getMyBookings = async (): Promise<PassengerBookingDTO[]> => {
  const res = await api.get<PassengerBookingDTO[]>(`/bookings/me`);
  return res.data;
};
