export interface Platforms {
    id: number;
    name: string;
    description: string;
    stock: number;
    price: number;
    s_type: ServiceType;
    link: string;
    is_active: boolean;
    created_at: Date;
}

export enum ServiceType {
    Video = 'Video',
    Music = 'Music',
    Game = 'Game'
}
