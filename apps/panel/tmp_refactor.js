const fs = require('fs');
const path = require('path');

const root = 'd:/Users/alexaver/Desktop/neversion/panel/src/app/features/services';
const compDir = path.join(root, 'components');
const prodsTableDir = path.join(compDir, 'products-table');
const servsTableDir = path.join(compDir, 'services-table');

const newProdModalDir = path.join(compDir, 'new-product-modal');
const servFormDir = path.join(compDir, 'service-form');

function replaceInFile(filePath, replacements) {
    if (!fs.existsSync(filePath)) return;
    let content = fs.readFileSync(filePath, 'utf8');
    for (const [search, replace] of replacements) {
        content = content.replace(search, replace);
    }
    fs.writeFileSync(filePath, content, 'utf8');
}

function moveDir(src, dest) {
    if (fs.existsSync(src)) {
        fs.renameSync(src, dest);
    }
}

// Rename folders
moveDir(prodsTableDir, servsTableDir);
moveDir(newProdModalDir, servFormDir);

// Rename files and replace contents for services-table
if (fs.existsSync(servsTableDir)) {
    ['ts', 'html'].forEach(ext => {
        const oldFile = path.join(servsTableDir, `products-table.component.${ext}`);
        const newFile = path.join(servsTableDir, `services-table.component.${ext}`);
        if (fs.existsSync(oldFile)) {
            fs.renameSync(oldFile, newFile);
            replaceInFile(newFile, [
                [/ProductsTableComponent/g, 'ServicesTableComponent'],
                [/app-products-table/g, 'app-services-table'],
                [/products-table/g, 'services-table'],
                [/Product/g, 'ServiceResponse'],
                [/product/g, 'service'],
                [/Inventory/g, 'Service'],
                [/inventory/g, 'service']
            ]);
        }
    });
}

// Rename files and replace contents for service-form
if (fs.existsSync(servFormDir)) {
    ['ts', 'html'].forEach(ext => {
        const oldFile = path.join(servFormDir, `new-product-modal.component.${ext}`);
        const newFile = path.join(servFormDir, `service-form.component.${ext}`);
        if (fs.existsSync(oldFile)) {
            fs.renameSync(oldFile, newFile);
            replaceInFile(newFile, [
                [/NewProductModalComponent/g, 'ServiceFormComponent'],
                [/app-new-product-modal/g, 'app-service-form'],
                [/new-product-modal/g, 'service-form'],
                [/ProductRequest/g, 'ServiceRequest'],
                [/Product/g, 'ServiceResponse'],
                [/product/g, 'service'],
                [/InventoryRequest/g, 'ServiceRequest'],
                [/inventory/g, 'service']
            ]);
        }
    });
}

// Fixing ServicesListComponent
const listCompFile = path.join(root, 'pages', 'services-list', 'services-list.component.ts');
const listHtmlFile = path.join(root, 'pages', 'services-list', 'services-list.component.html');

replaceInFile(listCompFile, [
    [/app-products/g, 'app-services-list'],
    [/ProductsComponent/g, 'ServicesListComponent'],
    [/products\.component\.html/g, 'services-list.component.html'],
    [/ProductsTableComponent/g, 'ServicesTableComponent'],
    [/products-table\/products-table/g, 'services-table/services-table'],
    [/NewProductModalComponent/g, 'ServiceFormComponent'],
    [/new-product-modal\/new-product-modal/g, 'service-form/service-form'],
    [/ProductService/g, 'ServicesDataService'],
    [/ProductRequest/g, 'ServiceRequest'],
    [/Product/g, 'ServiceResponse'],
    [/InventoryRequest/g, 'ServiceRequest'],
    [/productService/g, 'servicesDataService'],
    [/products/g, 'services'],
    [/product/g, 'service'],
    [/'\.\/components\//g, "'../../components/"],
    [/'\.\/services\//g, "'../../services/"],
    [/'\.\/models\//g, "'../../models/"]
]);

replaceInFile(listHtmlFile, [
    [/app-products-table/g, 'app-services-table'],
    [/app-new-product-modal/g, 'app-service-form'],
    [/productos/g, 'servicios'],
    [/Productos/g, 'Servicios'],
    [/producto/g, 'servicio'],
    [/Producto/g, 'Servicio']
]);

console.log('Done!');
