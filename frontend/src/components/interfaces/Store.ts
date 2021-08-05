export default interface Store {
    id: number;
    name: string;
    code: string;
    description: string;
    storeType: string;
    openingDate: string;
    additionalInfo: {specialField1: string, specialField2: string};
    seasons: Season[];
}

interface Season{
    id: number;
    season: string;
}