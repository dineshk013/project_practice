export type OrderStatus =
  | 'processing'
  | 'packed'
  | 'in_transit'
  | 'delivered'
  | 'cancelled';

export interface OrderItem {
  id: string;
  name: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: string;
  date: string;
  status: OrderStatus;
  items: OrderItem[];
  total: number;
  deliveryAddress: string;
}
